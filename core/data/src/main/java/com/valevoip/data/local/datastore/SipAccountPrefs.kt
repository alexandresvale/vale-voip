package com.valevoip.data.local.datastore

import kotlinx.serialization.Serializable

@Serializable
data class SipAccountPrefs(
    val username: String = "",
    val password: String = "",
    val domain: String = ""
)
