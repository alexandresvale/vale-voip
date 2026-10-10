package com.valevoip.data.security

import kotlinx.serialization.Serializable

@Serializable
data class SipAccountPrefs(
    val username: String = "",
    val password: String = "",
    val domain: String = ""
)
