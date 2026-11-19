package com.valevoip.core.sip

import com.valevoip.core.domain.model.AudioCodec
import org.linphone.core.PayloadType

internal fun PayloadType.toCodecId(): String = "${mimeType.uppercase()}/$clockRate/$channels"

internal fun PayloadType.toDomainAudioCodec(): AudioCodec {
    val normalizedMime = mimeType.uppercase()
    return AudioCodec(
        id = toCodecId(),
        mimeType = normalizedMime,
        clockRateHz = clockRate,
        channels = channels,
        description = resolveCodecDescription(normalizedMime, description),
        isEnabled = enabled()
    )
}

private fun resolveCodecDescription(mimeType: String, nativeDescription: String?): String {
    return when (mimeType) {
        "OPUS" -> "Alta definição adaptativa (Banda larga)"
        "PCMU" -> "G.711 µ-law (Padrão tradicional de telefonia)"
        "PCMA" -> "G.711 A-law (Padrão tradicional de telefonia)"
        "G722" -> "Voz em alta definição (Wideband)"
        "GSM" -> "Baixo consumo de banda (Full Rate)"
        "SPEEX" -> "Compressão de voz adaptativa para VoIP"
        "L16" -> "Áudio PCM linear sem compressão"
        else -> nativeDescription?.takeIf { it.isNotBlank() } ?: "Codec de áudio SIP/RTP"
    }
}
