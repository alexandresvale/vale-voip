package com.example.data.service

import android.content.Context
import org.linphone.core.AccountParams
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

    fun terminate() {
        core.stop()
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
}