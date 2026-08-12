package com.valevoip.core.sip

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import org.linphone.core.AudioDevice
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.LogLevel
import org.linphone.core.RegistrationState

internal class LinphoneSipClient(
    private val context: Context
) : SipClient {

    private val _registrationState = MutableStateFlow<SipRegistrationState>(SipRegistrationState.None)
    override val registrationState: StateFlow<SipRegistrationState> = _registrationState.asStateFlow()

    private val _callStatus = MutableStateFlow(CallStatus.IDLE)

    private var linphoneCore: Core? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        val factory = Factory.instance()
        factory.loggingService.domain = "ValeVoIP-Linphone"
        factory.loggingService.setLogLevel(LogLevel.Message)
        factory.enableLogcatLogs(true)

        linphoneCore = factory.createCore(null, null, context)

        linphoneCore?.isEchoCancellationEnabled = true
        linphoneCore?.disableRecordOnMute = true

        val natPolicy = linphoneCore?.createNatPolicy()
        natPolicy?.isIceEnabled = true
        natPolicy?.stunServer = "stun.linphone.org"

        linphoneCore?.natPolicy = natPolicy


        // Implementado o ouvido do Linphone para escutar as mudanças de status da rede
        linphoneCore?.addListener(object : CoreListenerStub() {
            override fun onAccountRegistrationStateChanged(
                core: Core,
                account: org.linphone.core.Account,
                state: RegistrationState?,
                message: String
            ) {
                Log.d("LinphoneSipClient", "RegistrationState = $state | Message = $message")
                when (state) {
                    RegistrationState.Ok -> _registrationState.value = SipRegistrationState.Ok
                    RegistrationState.Progress -> _registrationState.value = SipRegistrationState.Progress
                    RegistrationState.Cleared -> _registrationState.value = SipRegistrationState.Cleared
                    RegistrationState.Failed -> _registrationState.value = SipRegistrationState.Failed(reason = message)
                    RegistrationState.None -> _registrationState.value = SipRegistrationState.None
                    RegistrationState.Refreshing -> _registrationState.value = SipRegistrationState.Refreshing
                    else -> {}
                }
            }

            override fun onCallStateChanged(
                core: Core,
                call: org.linphone.core.Call,
                state: org.linphone.core.Call.State,
                message: String
            ) {
                Log.d("LinphoneSipClient", "CallState = $state | Message = $message")
                when (state) {
                    org.linphone.core.Call.State.OutgoingInit,
                    org.linphone.core.Call.State.OutgoingProgress -> _callStatus.value = CallStatus.DIALING

                    org.linphone.core.Call.State.OutgoingRinging -> _callStatus.value = CallStatus.RINGING
                    org.linphone.core.Call.State.IncomingReceived -> _callStatus.value = CallStatus.INCOMING
                    org.linphone.core.Call.State.Connected,
                    org.linphone.core.Call.State.StreamsRunning -> {
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        _callStatus.value = CallStatus.ACTIVE
                    }

                    org.linphone.core.Call.State.End,
                    org.linphone.core.Call.State.Error,
                    org.linphone.core.Call.State.Released -> {
                        audioManager.mode = AudioManager.MODE_NORMAL
                        setSpeakerphoneOn(false)
                        _callStatus.value = CallStatus.ENDED
                    }

                    else -> {}
                }
            }
        })
    }

    override suspend fun register(account: SipAccount): Flow<SipRegistrationState> {
        val core = linphoneCore ?: return flowOf(SipRegistrationState.Failed("Core is null"))

        _registrationState.value = SipRegistrationState.Progress

        val authInfo = Factory.instance().createAuthInfo(
            account.username, null, account.password, null, null, account.domain
        )

        val accountParams = core.createAccountParams()
        val identify = Factory.instance().createAddress("sip:${account.username}@${account.domain}")
        accountParams.identityAddress = identify

        // Define o endereço do servidor (Proxy)
        accountParams.serverAddress = Factory.instance().createAddress("sip:${account.domain}")
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
        Log.d("LinphoneSipClient", "RegistrationState register state $registrationState")
        return registrationState
    }

    override suspend fun unregister(): Flow<SipRegistrationState> {
        linphoneCore?.clearAccounts()
        linphoneCore?.clearAllAuthInfo()
        _registrationState.value = SipRegistrationState.None
        return flowOf(SipRegistrationState.None)
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

            var deviceChanged = false
            var newState = speakerEnabled

            // We can get a list of all available audio devices using audioDevices
            for (audioDevice in core.audioDevices) {
                if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                    call.outputAudioDevice = audioDevice
                    deviceChanged = true
                    newState = false
                    break
                } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                    call.outputAudioDevice = audioDevice
                    deviceChanged = true
                    newState = true
                    break
                }
            }

            if (deviceChanged) {
                Result.success(newState)
            } else {
                Result.failure(Exception("Dispositivo de áudio não suportado ou não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun setSpeakerphoneOn(on: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (on) {
                    val speakerDevice = audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                    speakerDevice?.let { audioManager.setCommunicationDevice(it) }
                } else {
                    audioManager.clearCommunicationDevice()
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = on
            }
        } catch (e: Exception) {
            Log.e("LinphoneSipClient", "Erro ao mudar viva-voz: ${e.message}")
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

    override fun getCallLogs(): Result<List<CallHistoryItem>> {
        return try {
            val logs = linphoneCore?.callLogs ?: return Result.success(emptyList())
            val mapped = logs.map { log ->
                val direction = when (log.dir) {
                    org.linphone.core.Call.Dir.Incoming -> CallDirection.INCOMING
                    else -> CallDirection.OUTGOING
                }

                val status = when (log.status) {
                    org.linphone.core.Call.Status.Missed -> CallHistoryStatus.MISSED
                    org.linphone.core.Call.Status.Declined,
                    org.linphone.core.Call.Status.Aborted -> CallHistoryStatus.DECLINED
                    else -> CallHistoryStatus.SUCCESS
                }

                CallHistoryItem(
                    id = log.callId ?: "",
                    remoteAddress = log.remoteAddress?.username ?: "Desconhecido",
                    displayName = log.remoteAddress?.displayName,
                    direction = direction,
                    status = status,
                    timestamp = log.startDate * 1000L,
                    durationSeconds = log.duration
                )
            }.sortedByDescending { it.timestamp }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun clearCallLogs(): Result<Unit> {
        return try {
            linphoneCore?.clearCallLogs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}