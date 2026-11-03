package com.valevoip.core.sip

import android.content.Context
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.core.domain.di.MainDispatcher
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.repository.CallHistoryRepository
import com.valevoip.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.LogLevel
import org.linphone.core.RegistrationState
import timber.log.Timber

internal class LinphoneSipClient(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + mainDispatcher)
) : SipClient, CallHistoryRepository {

    private val _registrationState = MutableStateFlow<SipRegistrationState>(SipRegistrationState.None)
    override val registrationState: StateFlow<SipRegistrationState> = _registrationState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerEnabled = MutableStateFlow(false)
    override val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled.asStateFlow()

    private val _callStatus = MutableStateFlow(CallStatus.IDLE)

    private var linphoneCore: Core? = null

    init {
        val factory = Factory.instance()
        factory.loggingService.domain = "ValeVoIP-Linphone"
        factory.loggingService.setLogLevel(LogLevel.Message)
        factory.enableLogcatLogs(true)

        linphoneCore = factory.createCore(null, null, context)

        linphoneCore?.disableRecordOnMute = true
        linphoneCore?.isVideoCaptureEnabled = false
        linphoneCore?.isVideoDisplayEnabled = false

        val natPolicy = linphoneCore?.createNatPolicy()
        natPolicy?.isIceEnabled = true
        natPolicy?.stunServer = "stun.linphone.org"

        linphoneCore?.natPolicy = natPolicy

        // Implementado o ouvido do Linphone para escutar as mudanças de status da rede e áudio
        linphoneCore?.addListener(object : CoreListenerStub() {
            override fun onAccountRegistrationStateChanged(
                core: Core,
                account: org.linphone.core.Account,
                state: RegistrationState?,
                message: String
            ) {
                Timber.d("RegistrationState = $state | Message = $message")
                when (state) {
                    RegistrationState.Ok -> _registrationState.value = SipRegistrationState.Ok
                    RegistrationState.Progress -> _registrationState.value = SipRegistrationState.Progress
                    RegistrationState.Cleared -> _registrationState.value = SipRegistrationState.Cleared
                    RegistrationState.Failed -> _registrationState.value = SipRegistrationState.Failed(reason = message)
                    RegistrationState.None -> _registrationState.value = SipRegistrationState.None
                    RegistrationState.Refreshing -> _registrationState.value = SipRegistrationState.Refreshing
                    null -> Unit
                }
            }

            override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
                val speakerActive = audioDevice.type == AudioDevice.Type.Speaker
                Timber.d("AudioDevice alterado: type=${audioDevice.type} | speaker=$speakerActive")
                _isSpeakerEnabled.value = speakerActive
            }

            override fun onLastCallEnded(core: Core) {
                Timber.d("onLastCallEnded: resetando estado de microfone e viva-voz")
                _isMuted.value = false
                _isSpeakerEnabled.value = false
            }

            override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State,
                message: String
            ) {
                Timber.d("CallState = $state | Message = $message")
                when (state) {
                    Call.State.OutgoingInit,
                    Call.State.OutgoingProgress -> _callStatus.value = CallStatus.DIALING

                    Call.State.OutgoingRinging -> _callStatus.value = CallStatus.RINGING
                    Call.State.IncomingReceived -> _callStatus.value = CallStatus.INCOMING
                    Call.State.Connected,
                    Call.State.StreamsRunning -> {
                        _isMuted.value = call.microphoneMuted
                        _isSpeakerEnabled.value = call.outputAudioDevice?.type == AudioDevice.Type.Speaker
                        _callStatus.value = CallStatus.ACTIVE
                    }

                    Call.State.End,
                    Call.State.Error,
                    Call.State.Released -> {
                        if (core.callsNb == 0) {
                            _isMuted.value = false
                            _isSpeakerEnabled.value = false
                        }
                        _callStatus.value = CallStatus.ENDED
                    }

                    else -> Unit
                }
            }
        })

        observeSettings()
    }

    private fun observeSettings() {
        // Sincroniza cancelamento e limitador de eco
        settingsRepository.settings
            .map { it.echoCancellationEnabled }
            .distinctUntilChanged()
            .onEach { enabled ->
                linphoneCore?.let { core ->
                    core.isEchoCancellationEnabled = enabled
                    core.isEchoLimiterEnabled = enabled
                }
            }
            .launchIn(coroutineScope)

        // Delega a política 'Usar Somente Wi-Fi' ao NetworkManagerAbove26 nativo da SDK Linphone
        settingsRepository.settings
            .map { it.useWifiOnly }
            .distinctUntilChanged()
            .onEach { enabled ->
                linphoneCore?.let { core ->
                    Timber.d("Sincronizando política WifiOnly nativa com Core: isWifiOnlyEnabled = $enabled")
                    core.isWifiOnlyEnabled = enabled
                }
            }
            .launchIn(coroutineScope)
    }

    override suspend fun register(account: SipAccount): Flow<SipRegistrationState> =
        withContext(mainDispatcher) {
            val core = linphoneCore ?: return@withContext flowOf(SipRegistrationState.Failed("Core is null"))
            val transportConfiguration = sipTransportConfiguration(account.transport)

            if (isAlreadyRegisteredOrConnecting(core, account, transportConfiguration)) {
                Timber.d(
                    "Registro idempotente: conta ${account.username}@${account.domain} já ativa/em progresso (${_registrationState.value})."
                )
                return@withContext registrationState
            }

            _registrationState.value = SipRegistrationState.Progress

            val authInfo = Factory.instance().createAuthInfo(
                account.username, null, account.password, null, null, account.domain
            )

            val accountParams = core.createAccountParams()
            // Define a identidade (Usuário)
            val identify = Factory.instance().createAddress("sip:${account.username}@${account.domain}")
            accountParams.identityAddress = identify

            core.verifyServerCertificates(transportConfiguration.verifyServerCertificates)
            core.verifyServerCn(transportConfiguration.verifyServerCommonName)

            // Preserve TCP for legacy servers; TLS is used only when explicitly selected.
            val serverAddress = Factory.instance().createAddress("sip:${account.domain}")
            serverAddress?.transport = transportConfiguration.linphoneTransport
            accountParams.serverAddress = serverAddress
            accountParams.isRegisterEnabled = true

            val linphoneAccount = core.createAccount(accountParams)
            // Limpa contas antigas, aplica credenciais e inicia o registro nativo
            core.clearAllAuthInfo()
            core.clearAccounts()

            core.addAuthInfo(authInfo)
            core.addAccount(linphoneAccount)
            core.defaultAccount = linphoneAccount

            // O start() faz com que a máquina interna do C++ do Linphone comece a agir
            core.start()
            Timber.d("RegistrationState register iniciado: ${_registrationState.value}")
            registrationState
        }

    private fun isAlreadyRegisteredOrConnecting(
        core: Core,
        account: SipAccount,
        transportConfiguration: SipTransportConfiguration
    ): Boolean {
        val currentAccount = core.defaultAccount ?: return false
        val identity = currentAccount.params.identityAddress
        val server = currentAccount.params.serverAddress

        val isSameAccount = identity?.username == account.username &&
            identity.domain == account.domain &&
            server?.transport == transportConfiguration.linphoneTransport

        val isHealthyOrConnecting = when (_registrationState.value) {
            SipRegistrationState.Ok,
            SipRegistrationState.Progress,
            SipRegistrationState.Refreshing -> true

            is SipRegistrationState.Failed,
            SipRegistrationState.Cleared,
            SipRegistrationState.None -> false
        }
        return isSameAccount && isHealthyOrConnecting
    }

    override suspend fun unregister(): Flow<SipRegistrationState> = withContext(mainDispatcher) {
        linphoneCore?.clearAccounts()
        linphoneCore?.clearAllAuthInfo()
        _registrationState.value = SipRegistrationState.None
        flowOf(SipRegistrationState.None)
    }

    override fun makeCall(number: String): Result<Unit> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val defaultProxy = core.defaultAccount ?: throw IllegalStateException("No default account")

            // Montar o endereço de destino com base no domínio da conta
            val domain = defaultProxy.params.serverAddress?.domain ?: ""
            val address = Factory.instance().createAddress("sip:$number@$domain")
                ?: throw IllegalArgumentException("Invalid address")

            core.inviteAddress(address)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun hangUp(): Result<Unit> {
        return try {
            val call = linphoneCore?.currentCall
            if (call != null) {
                call.terminate()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No active call to hang up"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun toggleMute(): Result<Unit> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = core.currentCall ?: throw IllegalStateException("No active call")
            call.microphoneMuted = !call.microphoneMuted
            _isMuted.value = call.microphoneMuted
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun toggleSpeaker(): Result<Boolean> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = core.currentCall ?: throw IllegalStateException("No active call")

            // Get the currently used audio device
            val currentAudioDevice = call.outputAudioDevice
            val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker
            val targetType = if (speakerEnabled) AudioDevice.Type.Earpiece else AudioDevice.Type.Speaker

            // We can get a list of all available audio devices using audioDevices
            val targetDevice = core.audioDevices.firstOrNull { it.type == targetType }
            if (targetDevice != null) {
                call.outputAudioDevice = targetDevice
                val newState = targetType == AudioDevice.Type.Speaker
                _isSpeakerEnabled.value = newState
                Result.success(newState)
            } else {
                Result.failure(Exception("Dispositivo de áudio não suportado ou não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun muteCall(callId: String): Result<Unit> {
        return toggleMute()
    }

    override fun acceptCall(): Result<Unit> {
        return try {
            val call = linphoneCore?.currentCall ?: throw IllegalStateException("No incoming call")
            call.accept()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCallStatusFlow(): Flow<CallStatus> = _callStatus.asStateFlow()

    override fun getCurrentCallNumber(): String? {
        return linphoneCore?.currentCall?.remoteAddress?.username
    }

    override fun getSynchronousCallStatus(): CallStatus = _callStatus.value

    override fun getLinphoneVersion(): String {
        return linphoneCore?.version ?: "Desconhecido"
    }

    private data class RawCallLogSnapshot(
        val id: String,
        val remoteUsername: String,
        val displayName: String?,
        val direction: Call.Dir,
        val status: Call.Status,
        val startDateSeconds: Long,
        val durationSeconds: Int
    )

    override suspend fun getCallHistory(): Result<List<CallHistoryItem>> {
        return try {
            // Extrai snapshot dos ponteiros JNI na thread confinada do Core
            val snapshots = withContext(mainDispatcher) {
                val logs = linphoneCore?.callLogs ?: return@withContext emptyList()
                logs.map { log ->
                    RawCallLogSnapshot(
                        id = log.callId ?: "",
                        remoteUsername = log.remoteAddress.username ?: "Desconhecido",
                        displayName = log.remoteAddress.displayName,
                        direction = log.dir,
                        status = log.status,
                        startDateSeconds = log.startDate,
                        durationSeconds = log.duration
                    )
                }
            }

            // Mapeia e ordena coleções no IoDispatcher sem bloquear a Main Thread
            val mapped = withContext(ioDispatcher) {
                snapshots.map { snapshot ->
                    val direction = when (snapshot.direction) {
                        Call.Dir.Incoming -> CallDirection.INCOMING
                        Call.Dir.Outgoing -> CallDirection.OUTGOING
                    }

                    val status = when (snapshot.status) {
                        Call.Status.Missed -> CallHistoryStatus.MISSED
                        Call.Status.Declined,
                        Call.Status.Aborted,
                        Call.Status.EarlyAborted -> CallHistoryStatus.DECLINED
                        Call.Status.Success,
                        Call.Status.AcceptedElsewhere,
                        Call.Status.DeclinedElsewhere -> CallHistoryStatus.SUCCESS
                    }

                    CallHistoryItem(
                        id = snapshot.id,
                        remoteAddress = snapshot.remoteUsername,
                        displayName = snapshot.displayName,
                        direction = direction,
                        status = status,
                        timestamp = snapshot.startDateSeconds * 1000L,
                        durationSeconds = snapshot.durationSeconds
                    )
                }.sortedByDescending { it.timestamp }
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCallHistoryItem(id: String): Result<Unit> = withContext(mainDispatcher) {
        try {
            val logToRemove = linphoneCore?.callLogs?.find { it.callId == id }
            if (logToRemove != null) {
                linphoneCore?.removeCallLog(logToRemove)
                Result.success(Unit)
            } else {
                Result.failure(NoSuchElementException("Call log com ID '$id' não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCallHistory(): Result<Unit> = withContext(mainDispatcher) {
        try {
            linphoneCore?.clearCallLogs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
