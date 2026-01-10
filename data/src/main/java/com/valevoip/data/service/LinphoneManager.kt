package com.valevoip.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.linphone.core.AccountListener
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.LogCollectionState
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinphoneManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val factory: Factory by lazy { Factory.instance() }
    private lateinit var core: Core

    init {
        initialize()
    }

    private fun initialize() {
        factory.enableLogcatLogs(true)
        val configPath = context.filesDir.absolutePath + "/.linphonerc"
        val factoryPath = context.filesDir.absolutePath + "/linphone_factory_rc"
        core = factory.createCore(configPath, factoryPath, context)
        core.enableLogCollection(LogCollectionState.Enabled)
        core.start()
    }

    fun stop() = core.stop()

    /**
     * Realiza o registro e retorna um Flow com o estado da tentativa.
     * O Manager cuida da criação de params, authInfo, etc.
     */
    fun registerAccount(username: String, domain: String, password: String): Flow<RegistrationState> = callbackFlow {
        clearExistingAccounts()

        val authInfo = factory.createAuthInfo(username, null, password, null, null, domain, null)

        val identity = factory.createAddress("sip:$username@$domain")
        val address = factory.createAddress("sip:$domain")?.apply { transport = TransportType.Udp }
        val params = core.createAccountParams().apply {
            identityAddress = identity
            serverAddress = address
            isRegisterEnabled = true
        }

        val account = core.createAccount(params)

        core.addAuthInfo(authInfo)
        core.addAccount(account)
        core.defaultAccount = account

        val listener = AccountListener { _, state, message ->
            logEvent("Registration State: $state | Msg: $message")
            trySend(state).isSuccess
        }

        account.addListener(listener)

        awaitClose {
            account.removeListener(listener)
        }
    }

    fun unregisterAccount(): Flow<RegistrationState> = callbackFlow {
        val account = core.defaultAccount
        if (account == null) {
            logEvent("[Account] Não há conta")
            close()
            return@callbackFlow
        }

        val params = account.params.clone()
        params.isRegisterEnabled = false
        account.params = params

        val listener = AccountListener { _, state, message ->
            logEvent("[Account] Unregister state changed: $state, $message")
            trySend(state).isSuccess
        }

        account.addListener(listener)
        awaitClose {
            logEvent("Unregister awaitClose")
            account.removeListener(listener)
        }
    }

    private fun clearExistingAccounts() {
        core.clearAccounts()
        core.clearAllAuthInfo()
    }

    fun invite(address: String): Boolean {
        // Garante que existe uma conta padrão para discar
        ensureDefaultAccount()

        // Habilita rede no core se necessário
        core.isNetworkReachable = true

        val call = core.invite(address)
        return call != null
    }

    fun terminateCurrentCall() {
        core.currentCall?.terminate() ?: run {
            if (core.callsNb > 0) core.terminateAllCalls()
        }
    }

    fun acceptCall() {
        core.currentCall?.accept()
    }

    fun delete() {
        val account = core.defaultAccount
        account?.let {
            core.removeAccount(it)
            core.clearAccounts()
            core.clearAllAuthInfo()
        }
    }

    /**
     * Alterna o estado do microfone.
     * Retorna o novo estado (true = mutado, false = ouvindo).
     * Nota: Na SDK da Linphone, 'isMicEnabled = true' significa que o mic está ABERTO (não mutado).
     */
    fun toggleMicrophone() {
        core.isMicEnabled = !core.isMicEnabled
        logEvent("Mic alterado. Mutado: ${core.isMicEnabled}")
    }

    /**
     * Alterna entre Viva-voz (Speaker) e Earpiece (Ouvido).
     * Retorna true se o Speaker ficou ativo, false caso contrário.
     */
    fun toggleSpeaker(): Boolean {
        val currentCall = core.currentCall ?: return false

        // Descobre qual dispositivo está em uso agora
        val currentDevice = currentCall.outputAudioDevice
        val isSpeakerNow = currentDevice?.type == AudioDevice.Type.Speaker

        // Define o alvo (Se tá Speaker, vai pra Earpiece, e vice-versa)
        val targetType = if (isSpeakerNow) AudioDevice.Type.Earpiece else AudioDevice.Type.Speaker

        // Busca o dispositivo na lista de hardwares disponíveis do Android
        val targetDevice = core.audioDevices.find { it.type == targetType }

        // Se achou o dispositivo, aplica.
        if (targetDevice != null) {
            currentCall.outputAudioDevice = targetDevice
            return targetType == AudioDevice.Type.Speaker
        } else {
            // Fallback: Se não achou Earpiece (ex: Tablet sem saida de ouvido), tenta Bluetooth ou Aux
            logEvent("Dispositivo de áudio $targetType não encontrado.")
            return isSpeakerNow // Mantém estado atual
        }
    }

    /**
     * Verifica se o Speaker está ativo.
     */
    fun isSpeakerEnabled(): Boolean {
        val currentCall = core.currentCall ?: return false
        return currentCall.outputAudioDevice?.type == AudioDevice.Type.Speaker
    }

    fun observeCoreCallState(): Flow<Call.State> = callbackFlow {
        val listener = object : CoreListenerStub() {
            override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
                Log.d(
                    "LinphoneManager",
                    "State: $state | Msg: $message | Call: ${call.core.currentCallRemoteAddress?.toString()}"
                )
                trySend(state).isSuccess
            }
        }

        core.addListener(listener)
        awaitClose {
            Log.d("LinphoneManager", "Flow observeCallState cancelado")
            core.removeListener(listener)
        }
    }

    fun getCurrentCallNumber(): String? {
        val call = core.currentCall ?: core.calls.firstOrNull()
        Log.d("LinphoneManager", "getCurrentCallNumber = ${call?.remoteAddress}")
        return call?.remoteAddress?.username
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 1. Obtém a rede ativa no momento (Wifi, Dados, VPN, etc)
        val network = connectivityManager.activeNetwork ?: return false

        // 2. Obtém as capacidades dessa rede (Velocidade, acesso à internet, etc)
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        // 3. Verificação Rigorosa:
        // NET_CAPABILITY_INTERNET: Significa que a rede foi configurada para acessar a internet (não é apenas uma LAN local)
        // NET_CAPABILITY_VALIDATED: (Opcional) Significa que o Android pingou o Google e confirmou que há dados reais fluindo.
        return when {
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> true
            else -> false
        }
    }

    private fun ensureDefaultAccount() {
        if (core.defaultAccount == null && core.accountList.isNotEmpty()) {
            core.defaultAccount = core.accountList.first()
        }
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "LinphoneManager | $string")
    }
}