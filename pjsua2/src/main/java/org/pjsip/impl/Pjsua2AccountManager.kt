package org.pjsip.impl

import android.util.Log
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pjsip_transport_type_e
import org.pjsip.pjsua2.pjsua_state

class Pjsua2AccountManager(
    private val endpoint: Endpoint
) {

    private var account: MyAccount? = null
    private var eventListener: AccountListener? = null

    private fun startPjsua2() {
        try {
            val transportConfig = TransportConfig().apply { port = 5060 }

            with(endpoint) {
                if (libGetState() > pjsua_state.PJSUA_STATE_NULL) {
                    Log.d("####", "${this.javaClass.name} Start = ${endpoint.libGetState()}")
                    return
                }
                libCreate()
                libInit(EpConfig())
                transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, transportConfig)
                libStart()
            }

            Log.d("####", "${this.javaClass.name} PJSUA2 Iniciado com sucesso!")
        } catch (exception: Exception) {
            Log.d("####", "${this.javaClass.name} startPjsua2 = $exception")
        }
    }

    fun registerAccount(userName: String, password: String, domain: String) {
        startPjsua2()
        val accountConfig = createAccountConfig(userName, domain, password)
        account = MyAccount()
        try {
            account?.create(accountConfig)
        } catch (exception: Exception) {
            Log.d("####", "${this.javaClass.name} registerAccount = $exception")
        }
    }

    fun unregisterAccount() {
        account?.delete()
        account = null
        endpoint.libDestroy()
    }

    fun call(recipient: String) {
        val callParam = CallOpParam(true) // `true` indica que a chamada está saindo
        val call = MyAccount() // Implementação de uma classe que herda `Call`
//        call.makeCall("sip:destinatario@sip_server.com", callParam)
    }

    fun setAccountEventListener(listener: AccountListener) {
        eventListener = listener
        account?.setEventListener(listener)
    }

    fun removeAccountListener() {
        TODO("Not yet implemented")
    }

    private fun createAccountConfig(userName: String, domain: String, password: String): AccountConfig {
        return AccountConfig().apply {
            idUri = "sip:$userName@$domain"
            regConfig.registrarUri = "sip:$domain"
            sipConfig.authCreds.add(AuthCredInfo("digest", "*", userName, 0, password))
        }
    }

}
