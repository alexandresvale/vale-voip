package com.valevoip.core.sip

import android.content.Context
import android.content.pm.ApplicationInfo
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.core.domain.di.MainDispatcher
import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.AudioRouteType
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
import org.linphone.core.MediaEncryption
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

    private val _availableAudioRoutes = MutableStateFlow<List<AudioRoute>>(emptyList())
    override val availableAudioRoutes: StateFlow<List<AudioRoute>> = _availableAudioRoutes.asStateFlow()

    private val _activeAudioRoute = MutableStateFlow<AudioRoute?>(null)
    override val activeAudioRoute: StateFlow<AudioRoute?> = _activeAudioRoute.asStateFlow()

    private val _callStatus = MutableStateFlow(CallStatus.IDLE)

    private var linphoneCore: Core? = null

    init {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val factory = Factory.instance()
        factory.loggingService.domain = "ValeVoIP-Linphone"
        factory.loggingService.setLogLevel(if (isDebuggable) LogLevel.Message else LogLevel.Fatal)
        factory.enableLogcatLogs(isDebuggable)

        linphoneCore = factory.createCore(null, null, context)

        linphoneCore?.disableRecordOnMute = true
        linphoneCore?.isVideoCaptureEnabled = false
        linphoneCore?.isVideoDisplayEnabled = false

        val natPolicy = linphoneCore?.createNatPolicy()
        natPolicy?.isStunEnabled = true
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

            override fun onAudioDevicesListUpdated(core: Core) {
                Timber.d("Lista de dispositivos de áudio atualizada pelo sistema")
                refreshAudioRoutes(core)
            }

            override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
                Timber.d("AudioDevice alterado: type=${audioDevice.type} | name=${audioDevice.deviceName}")
                refreshAudioRoutes(core, explicitActiveDevice = audioDevice)
            }

            override fun onLastCallEnded(core: Core) {
                Timber.d("onLastCallEnded: resetando estado de microfone e viva-voz")
                _isMuted.value = false
                _isSpeakerEnabled.value = false
                refreshAudioRoutes(core)
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
                    Call.State.OutgoingProgress -> {
                        refreshAudioRoutes(core)
                        _callStatus.value = CallStatus.DIALING
                    }

                    Call.State.OutgoingRinging,
                    Call.State.OutgoingEarlyMedia -> {
                        refreshAudioRoutes(core)
                        _callStatus.value = CallStatus.RINGING
                    }

                    Call.State.IncomingReceived,
                    Call.State.IncomingEarlyMedia -> {
                        refreshAudioRoutes(core)
                        _callStatus.value = CallStatus.INCOMING
                    }

                    Call.State.Connected,
                    Call.State.StreamsRunning,
                    Call.State.PausedByRemote,
                    Call.State.Updating,
                    Call.State.UpdatedByRemote -> {
                        _isMuted.value = call.microphoneMuted
                        refreshAudioRoutes(core, explicitActiveDevice = call.outputAudioDevice)
                        _callStatus.value = CallStatus.ACTIVE
                    }

                    Call.State.Pausing,
                    Call.State.Paused,
                    Call.State.Resuming -> {
                        _isMuted.value = call.microphoneMuted
                        refreshAudioRoutes(core, explicitActiveDevice = call.outputAudioDevice)
                        _callStatus.value = CallStatus.PAUSED
                    }

                    Call.State.End,
                    Call.State.Error,
                    Call.State.Released -> {
                        if (core.callsNb == 0) {
                            _isMuted.value = false
                            _isSpeakerEnabled.value = false
                        }
                        refreshAudioRoutes(core)
                        _callStatus.value = CallStatus.ENDED
                    }

                    Call.State.Idle,
                    Call.State.PushIncomingReceived,
                    Call.State.Referred,
                    Call.State.EarlyUpdatedByRemote,
                    Call.State.EarlyUpdating -> Unit
                }
            }
        })

        observeSettings()
        linphoneCore?.let { refreshAudioRoutes(it) }
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

        // Sincroniza criptografia de mídia (SRTP) sob demanda
        settingsRepository.settings
            .map { it.srtpEnabled }
            .distinctUntilChanged()
            .onEach { enabled ->
                linphoneCore?.let { core ->
                    val encryption = if (enabled) MediaEncryption.SRTP else MediaEncryption.None
                    Timber.d("Sincronizando criptografia de mídia (SRTP) com Core: mediaEncryption = $encryption")
                    core.mediaEncryption = encryption
                    core.isMediaEncryptionMandatory = false
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

            // Configura o servidor SIP com o transporte selecionado (UDP, TCP ou TLS) e porta customizada quando aplicável.
            val serverAddress = Factory.instance().createAddress("sip:${account.domain}")
            serverAddress?.transport = transportConfiguration.linphoneTransport
            if (shouldApplyExplicitPort(account)) {
                serverAddress?.port = account.port
            }
            accountParams.serverAddress = serverAddress
            accountParams.isOutboundProxyEnabled = true
            accountParams.natPolicy = core.natPolicy
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

        val isSamePort = if (shouldApplyExplicitPort(account)) {
            server?.port == account.port
        } else {
            server?.port == -1 || server?.port == 0 || server?.port == account.port
        }

        val isSameAccount = identity?.username == account.username &&
            identity.domain == account.domain &&
            isSamePort &&
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
            val defaultAccount = core.defaultAccount ?: throw IllegalStateException("No default account")
            val cleanTarget = number.trim()
            if (cleanTarget.isBlank()) {
                throw IllegalArgumentException("Destino da chamada não pode ser vazio")
            }

            // interpretUrl resolve ramais numéricos (2525), usernames alfanuméricos (valevoip, alexandreskt16)
            // aplicando o domínio da conta padrão, além de URIs SIP completas (usuario@dominio).
            val address = core.interpretUrl(cleanTarget, false)
                ?: run {
                    val domain = defaultAccount.params.serverAddress?.domain.orEmpty()
                    Factory.instance().createAddress("sip:$cleanTarget@$domain")
                }
                ?: throw IllegalArgumentException("Endereço SIP inválido: $cleanTarget")

            core.inviteAddress(address)
                ?: throw IllegalStateException("Não foi possível iniciar a chamada para $cleanTarget")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun hangUp(): Result<Unit> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = resolveActiveOrPausedCall(core)
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
            val call = resolveActiveOrPausedCall(core) ?: throw IllegalStateException("No active call")
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
            val call = resolveActiveOrPausedCall(core)
            val currentAudioDevice = call?.outputAudioDevice ?: core.outputAudioDevice
            val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker
            val targetType = if (speakerEnabled) AudioDevice.Type.Earpiece else AudioDevice.Type.Speaker

            val targetDevice = core.audioDevices.firstOrNull { device ->
                device.type == targetType && device.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
            }
            if (targetDevice != null) {
                applyAudioRoute(core = core, call = call, targetOutputDevice = targetDevice)
                val newState = targetType == AudioDevice.Type.Speaker
                Result.success(newState)
            } else {
                Result.failure(Exception("Dispositivo de áudio não suportado ou não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun selectAudioRoute(routeId: String): Result<Unit> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = resolveActiveOrPausedCall(core)
            val targetDevice = core.audioDevices.firstOrNull { device ->
                device.id == routeId && device.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
            } ?: throw NoSuchElementException("Rota de áudio '$routeId' não disponível.")

            applyAudioRoute(core = core, call = call, targetOutputDevice = targetDevice)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun sendDtmf(digit: Char): Result<Unit> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = resolveActiveOrPausedCall(core)
                ?: throw IllegalStateException("Nenhuma chamada ativa para envio de DTMF.")

            core.playDtmf(digit, DTMF_TONE_DURATION_MS)
            val resultCode = call.sendDtmf(digit)
            if (resultCode != 0) {
                throw IllegalStateException("Falha ao enviar dígito DTMF '$digit' (código=$resultCode).")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun toggleHold(): Result<Boolean> {
        return try {
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = resolveActiveOrPausedCall(core)
                ?: throw IllegalStateException("Nenhuma chamada ativa para alternar espera.")

            when (call.state) {
                Call.State.Paused -> {
                    val resultCode = call.resume()
                    if (resultCode != 0) {
                        throw IllegalStateException("Falha ao retomar a chamada (código=$resultCode).")
                    }
                    Result.success(false)
                }

                Call.State.StreamsRunning,
                Call.State.Connected,
                Call.State.UpdatedByRemote -> {
                    val resultCode = call.pause()
                    if (resultCode != 0) {
                        throw IllegalStateException("Falha ao colocar a chamada em espera (código=$resultCode).")
                    }
                    Result.success(true)
                }

                Call.State.Pausing -> Result.success(true)
                Call.State.Resuming -> Result.success(false)

                Call.State.Idle,
                Call.State.IncomingReceived,
                Call.State.PushIncomingReceived,
                Call.State.OutgoingInit,
                Call.State.OutgoingProgress,
                Call.State.OutgoingRinging,
                Call.State.OutgoingEarlyMedia,
                Call.State.PausedByRemote,
                Call.State.Updating,
                Call.State.End,
                Call.State.Released,
                Call.State.Error,
                Call.State.Referred,
                Call.State.IncomingEarlyMedia,
                Call.State.EarlyUpdatedByRemote,
                Call.State.EarlyUpdating -> throw IllegalStateException(
                    "A chamada não pode alternar espera no estado atual (${call.state})."
                )
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
            val core = linphoneCore ?: throw IllegalStateException("Core is null")
            val call = resolveActiveOrPausedCall(core) ?: throw IllegalStateException("No incoming call")
            call.accept()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCallStatusFlow(): Flow<CallStatus> = _callStatus.asStateFlow()

    override fun getCurrentCallNumber(): String? {
        val core = linphoneCore ?: return null
        return resolveActiveOrPausedCall(core)?.remoteAddress?.username
    }

    override fun getSynchronousCallStatus(): CallStatus = _callStatus.value

    override fun getLinphoneVersion(): String {
        return linphoneCore?.version ?: "Desconhecido"
    }

    /**
     * Na liblinphone, [Core.getCurrentCall] retorna null quando a única chamada existente está em
     * [Call.State.Paused]. Este helper garante que operações de retomada, desligamento, DTMF e áudio
     * localizem a chamada mesmo enquanto pausada.
     */
    private fun resolveActiveOrPausedCall(core: Core): Call? {
        return core.currentCall ?: core.calls.firstOrNull()
    }

    private fun applyAudioRoute(core: Core, call: Call?, targetOutputDevice: AudioDevice) {
        if (call != null) {
            call.outputAudioDevice = targetOutputDevice
        } else {
            core.outputAudioDevice = targetOutputDevice
        }

        // Sincroniza o dispositivo de captura (microfone) correspondente para evitar captação do bolso em fones Bluetooth/USB
        val matchingInput = resolveMatchingInputDevice(core, targetOutputDevice)
        if (matchingInput != null) {
            if (call != null) {
                call.inputAudioDevice = matchingInput
            } else {
                core.inputAudioDevice = matchingInput
            }
        }

        refreshAudioRoutes(core, explicitActiveDevice = targetOutputDevice)
    }

    private fun resolveMatchingInputDevice(core: Core, targetOutputDevice: AudioDevice): AudioDevice? {
        if (targetOutputDevice.hasCapability(AudioDevice.Capabilities.CapabilityRecord)) {
            return targetOutputDevice
        }

        val recordDevices = core.extendedAudioDevices.filter {
            it.hasCapability(AudioDevice.Capabilities.CapabilityRecord)
        }

        return when (targetOutputDevice.type) {
            AudioDevice.Type.Bluetooth,
            AudioDevice.Type.HearingAid,
            AudioDevice.Type.Headset,
            AudioDevice.Type.GenericUsb -> {
                recordDevices.firstOrNull {
                    it.type == targetOutputDevice.type && it.deviceName == targetOutputDevice.deviceName
                } ?: recordDevices.firstOrNull {
                    it.type == targetOutputDevice.type
                } ?: recordDevices.firstOrNull {
                    it.type == AudioDevice.Type.Microphone
                }
            }

            AudioDevice.Type.Earpiece,
            AudioDevice.Type.Speaker,
            AudioDevice.Type.Headphones,
            AudioDevice.Type.Unknown,
            AudioDevice.Type.Microphone,
            AudioDevice.Type.BluetoothA2DP,
            AudioDevice.Type.Telephony,
            AudioDevice.Type.AuxLine,
            AudioDevice.Type.Hdmi -> {
                recordDevices.firstOrNull { it.type == AudioDevice.Type.Microphone }
            }
        }
    }

    private fun refreshAudioRoutes(core: Core, explicitActiveDevice: AudioDevice? = null) {
        val routes = core.audioDevices
            .filter { it.hasCapability(AudioDevice.Capabilities.CapabilityPlay) }
            .mapNotNull { it.toDomainAudioRoute() }
            .distinctBy { it.id }
            .sortedBy { it.type.sortOrder() }

        _availableAudioRoutes.value = routes

        val currentOutputDevice = explicitActiveDevice?.takeIf {
            it.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
        }
            ?: resolveActiveOrPausedCall(core)?.outputAudioDevice
            ?: core.outputAudioDevice
            ?: core.defaultOutputAudioDevice

        val resolvedActiveRoute = currentOutputDevice?.toDomainAudioRoute()?.let { candidate ->
            routes.firstOrNull { it.id == candidate.id } ?: candidate
        }
            ?: routes.firstOrNull { it.type == AudioRouteType.EARPIECE }
            ?: routes.firstOrNull()

        _activeAudioRoute.value = resolvedActiveRoute
        _isSpeakerEnabled.value = resolvedActiveRoute?.type == AudioRouteType.SPEAKER
    }

    private fun AudioDevice.toDomainAudioRoute(): AudioRoute? {
        return when (type) {
            AudioDevice.Type.Earpiece -> AudioRoute(
                id = id,
                name = "Telefone",
                type = AudioRouteType.EARPIECE
            )

            AudioDevice.Type.Speaker -> AudioRoute(
                id = id,
                name = "Viva-voz",
                type = AudioRouteType.SPEAKER
            )

            AudioDevice.Type.Bluetooth,
            AudioDevice.Type.HearingAid -> AudioRoute(
                id = id,
                name = deviceName.ifBlank { "Bluetooth" },
                type = AudioRouteType.BLUETOOTH
            )

            AudioDevice.Type.Headset,
            AudioDevice.Type.Headphones,
            AudioDevice.Type.GenericUsb -> AudioRoute(
                id = id,
                name = deviceName.ifBlank { "Fone de ouvido" },
                type = AudioRouteType.WIRED_HEADSET
            )

            AudioDevice.Type.Unknown,
            AudioDevice.Type.Microphone,
            AudioDevice.Type.BluetoothA2DP,
            AudioDevice.Type.Telephony,
            AudioDevice.Type.AuxLine,
            AudioDevice.Type.Hdmi -> null
        }
    }

    private fun AudioRouteType.sortOrder(): Int = when (this) {
        AudioRouteType.BLUETOOTH -> 0
        AudioRouteType.WIRED_HEADSET -> 1
        AudioRouteType.EARPIECE -> 2
        AudioRouteType.SPEAKER -> 3
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

    private fun shouldApplyExplicitPort(account: SipAccount): Boolean {
        return account.port != SipAccount.DEFAULT_SIP_PORT || IPV4_REGEX.matches(account.domain)
    }

    private companion object {
        const val DTMF_TONE_DURATION_MS = 120
        val IPV4_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
    }
}
