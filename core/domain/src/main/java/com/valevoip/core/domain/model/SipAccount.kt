package com.valevoip.core.domain.model

data class SipAccount(
    val username: String,
    val password: String,
    val domain: String,
    val transport: SipTransport = SipTransport.TCP
)