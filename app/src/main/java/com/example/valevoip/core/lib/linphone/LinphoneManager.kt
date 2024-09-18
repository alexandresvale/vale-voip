package com.example.valevoip.core.lib.linphone


import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import javax.inject.Inject

class LinphoneManager @Inject constructor(
    private val context: Context
) {
    private val factory = Factory.instance()
    private lateinit var core: Core

    // Flow para emitir os eventos de chamadas
    private val _callStateFlow = MutableStateFlow<Call.State?>(null)
    val callStateFlow = _callStateFlow.asStateFlow()

    // Flow para emitir os eventos de registro
    private val _registrationStateFlow = MutableStateFlow<RegistrationState?>(null)
    val registrationStateFlow = _registrationStateFlow.asStateFlow()

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            super.onAccountRegistrationStateChanged(core, account, state, message)
            logEvent("Message = $message")
            logEvent("State = $state")
            _registrationStateFlow.value = state
        }

        override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
            super.onAudioDeviceChanged(core, audioDevice)
            logEvent("core = ${core.version}")
            logEvent("audioDevice - device = ${audioDevice.deviceName} / id = ${audioDevice.id}")
        }

        override fun onAudioDevicesListUpdated(core: Core) {
            super.onAudioDevicesListUpdated(core)
        }


        override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
            super.onCallStateChanged(core, call, state, message)
            _callStateFlow.value = state
            when (state) {
                Call.State.IncomingReceived -> {
                    logEvent("Entrada Recebida - Chamando...")
                }

                Call.State.Connected -> {
                    logEvent("Conectada - Aceitou ligação")
                }

                Call.State.Released -> logEvent("Lançada - Pronta")
                Call.State.Idle -> logEvent("Idle")
                Call.State.PushIncomingReceived -> logEvent("PushIncomingReceived")
                Call.State.OutgoingInit -> logEvent("OutgoingInit")
                Call.State.OutgoingProgress -> logEvent("OutgoingProgress")
                Call.State.OutgoingRinging -> logEvent("OutgoingRinging")
                Call.State.OutgoingEarlyMedia -> logEvent("OutgoingEarlyMedia")
                Call.State.StreamsRunning -> {
                    logEvent("StreamsRunning - Chamada em andamento.")
                }

                Call.State.Pausing -> logEvent("Pausing")
                Call.State.Paused -> logEvent("Paused")
                Call.State.Resuming -> logEvent("Resuming")
                Call.State.Referred -> logEvent("Referred")
                Call.State.Error -> logEvent("Error")
                Call.State.End -> {
                    logEvent("End - Ligação caiu..")
                }

                Call.State.PausedByRemote -> logEvent("PausedByRemote")
                Call.State.UpdatedByRemote -> logEvent("UpdatedByRemote")
                Call.State.IncomingEarlyMedia -> logEvent("IncomingEarlyMedia")
                Call.State.Updating -> logEvent("Updating")
                Call.State.EarlyUpdatedByRemote -> logEvent("EarlyUpdatedByRemote")
                Call.State.EarlyUpdating -> logEvent("EarlyUpdating")
                null -> logEvent("null")
            }
        }
    }

    init {
        startLinphone()
    }

    private fun startLinphone() {
        factory.enableLogcatLogs(true)
        core = Factory.instance().createCore(null, null, context)
        core.addListener(coreListener)
        core.start()
    }

    fun registerAccount(username: String, password: String, domain: String, callback: (RegistrationState) -> Unit) {
        val authInfo = factory.createAuthInfo(
            username, null, password, null, null, domain, null
        )

        val accountParams = getAccountParams(username, domain)
        val account = core.createAccount(accountParams)

        with(core) {
            addAuthInfo(authInfo)
            addAccount(account)
            defaultAccount = account
        }

        account.addListener { account, state, message ->
            logEvent("[Account] Registration state changed: $state, $message")
            state?.let { callback(it) }
        }
        logEvent("Linphone Core ${core.version}")
    }


    fun unregister(callback: (RegistrationState) -> Unit) {
        val account = core.defaultAccount
        account ?: return
        val clonedParams = account.params.clone()
        clonedParams.isRegisterEnabled = false
        account.params = clonedParams
        account.addListener { _, state, message ->
            logEvent("[Account] Unregister state changed: $state, $message")
            state?.let(callback)
        }
    }

    fun delete() {
        val account = core.defaultAccount
        account ?: return
        core.removeAccount(account)
        core.clearAccounts()
        core.clearAllAuthInfo()
    }

    fun makeCall(callee: String, onCallStateChanged: (Call.State) -> Unit) {
        try {
            val params = core.createCallParams(null)
            val address = core.createAddress(callee)
        } catch (e: Exception) {
        }

    }

    fun accept() {
        core.currentCall?.accept()
        logEvent("accept isMicEnabled = ${core.isMicEnabled}")
    }

    fun terminate() {
        core.currentCall?.terminate()
    }

    fun micEnabled() {
        core.isMicEnabled = !core.isMicEnabled
    }

    fun toggleSpeaker() {
        val currentAudioDevice = core.currentCall?.outputAudioDevice
        val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker

        for (audioDevice in core.audioDevices) {
            if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            }
        }
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "LinphoneManager | $string")
    }

    private fun getAccountParams(
        username: String,
        domain: String
    ): AccountParams {
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        val address = Factory.instance().createAddress("sip:$domain")?.apply {
            transport = TransportType.Udp
        }

        return core.createAccountParams().apply {
            identityAddress = identity
            serverAddress = address
            isRegisterEnabled = true
        }
    }
}
