package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class SetAudioCodecEnabledUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(codecId: String, enabled: Boolean): Result<Unit> {
        val cleanId = codecId.trim()
        if (cleanId.isBlank()) {
            return Result.failure(IllegalArgumentException("Identificador do codec não pode ser vazio."))
        }

        if (!enabled) {
            val enabledCodecs = sipClient.availableAudioCodecs.value.filter { it.isEnabled }
            val isDisablingLastEnabledCodec = enabledCodecs.size <= 1 && enabledCodecs.any { it.id == cleanId }
            if (isDisablingLastEnabledCodec) {
                return Result.failure(
                    IllegalStateException("Mantenha pelo menos um codec de áudio habilitado.")
                )
            }
        }

        return sipClient.setAudioCodecEnabled(cleanId, enabled)
    }
}
