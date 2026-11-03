package com.valevoip.core.domain.model

data class VoipSettings(
    val echoCancellationEnabled: Boolean = true,
    val vibrateOnIncomingCall: Boolean = true,
    val useWifiOnly: Boolean = false
)
