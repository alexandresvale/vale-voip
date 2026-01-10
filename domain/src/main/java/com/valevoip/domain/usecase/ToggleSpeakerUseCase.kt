package com.valevoip.domain.usecase

import com.valevoip.domain.repository.ValeVoipService
import javax.inject.Inject

class ToggleSpeakerUseCase @Inject constructor(
    private val service: ValeVoipService
) {
    /**
     * Alterna a saída de áudio entre o Fone (Earpiece) e o Alto-falante (Speaker).
     */
    operator fun invoke(): Result<Boolean> {
        return service.toggleSpeaker()
    }
}