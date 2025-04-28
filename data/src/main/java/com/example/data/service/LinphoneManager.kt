package com.example.data.service

import android.content.Context
import android.util.Log
import org.linphone.core.AccountParams
import org.linphone.core.AudioDevice
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.TransportType

class LinphoneManager(
    private val context: Context
) {

    val factory = Factory.instance()
    private lateinit var core: Core

    init {
        initialize()
    }

    private fun initialize() {
        factory.enableLogcatLogs(true)
        core = Factory.instance().createCore(null, null, context)
//        core.addListener(coreListener)
        core.start()
    }

    fun stop() {
        core.stop()
//        core.removeListener(coreListener)
    }

    fun getCore(): Core = core

    fun getAccountParams(
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

    fun delete() {
        val account = core.defaultAccount
        account?.let {
            core.removeAccount(it)
            core.clearAccounts()
            core.clearAllAuthInfo()
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
}