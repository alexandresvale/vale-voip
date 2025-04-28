package org.pjsip.impl

interface AccountListener {
    fun onIncomingCall(callId: Int)
    fun onRegistrationState(state: String)
}