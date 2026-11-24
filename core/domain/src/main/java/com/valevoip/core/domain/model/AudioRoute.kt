package com.valevoip.core.domain.model

enum class AudioRouteType {
    EARPIECE,
    SPEAKER,
    BLUETOOTH,
    WIRED_HEADSET
}

data class AudioRoute(
    val id: String,
    val name: String,
    val type: AudioRouteType
)
