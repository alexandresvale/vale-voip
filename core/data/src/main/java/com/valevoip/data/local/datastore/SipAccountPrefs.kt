package com.valevoip.data.local.datastore

import com.valevoip.core.domain.model.SipTransport
import kotlinx.serialization.Serializable

@Serializable
data class SipAccountPrefs(
    val username: String = "",
    val password: String = "",
    val domain: String = ""
    val transport: String = SipTransport.TCP.name
)
