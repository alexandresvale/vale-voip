package com.valevoip.core.domain.model

data class AudioCodec(
    val id: String,
    val mimeType: String,
    val clockRateHz: Int,
    val channels: Int,
    val description: String,
    val isEnabled: Boolean
) {
    val formattedClockRate: String
        get() = if (clockRateHz % 1000 == 0) {
            "${clockRateHz / 1000} kHz"
        } else {
            "$clockRateHz Hz"
        }

    val displayLabel: String
        get() = "$mimeType • $formattedClockRate"
}
