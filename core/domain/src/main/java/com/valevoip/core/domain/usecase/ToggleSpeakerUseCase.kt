package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class ToggleSpeakerUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    /**
     * Alterna a saída de áudio entre o Fone (Earpiece) e o Alto-falante (Speaker).
     */
    operator fun invoke(): Result<Boolean> {
        return sipClient.toggleSpeaker()
    }
}