package org.pjsip.impl

import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnRegStateParam

class MyAccount : Account() {

    private var eventListener: AccountListener? = null

    fun setEventListener(listener: AccountListener) {
        this.eventListener = listener
    }

    override fun onRegState(prm: OnRegStateParam?) {
        super.onRegState(prm)
        val state = prm?.code.toString()
        eventListener?.onRegistrationState(state)
        println(
            "Estado de registro:\n" +
                    "Código = ${prm?.code}\n" +
                    "Status = ${prm?.status}\n" +
                    "Motivo = ${prm?.reason}\n" +
                    "Src Address = ${prm?.rdata?.srcAddress}\n" +
                    "Info = ${prm?.rdata?.info}\n" +
                    "Whole Msg = ${prm?.rdata?.wholeMsg}\n"
        )
        println("Estado de registro: ${prm?.code} ${prm?.reason}")
    }

    // Sobrescreva outros métodos para lidar com eventos SIP, como chamadas recebidas
    override fun onIncomingCall(prm: OnIncomingCallParam?) {
        super.onIncomingCall(prm)
        val callId = prm?.callId ?: return
        eventListener?.onIncomingCall(callId)
        println("Recebendo chamada de ${prm.rdata?.info.toString()}")
    }
}
