package com.valevoip.core.domain.model

data class SipAccount(
    val username: String,
    val password: String,
    val domain: String,
    val port: Int = DEFAULT_SIP_PORT,
    val transport: SipTransport = SipTransport.TCP
) {
    companion object {
        const val DEFAULT_SIP_PORT = 5060
    }
}