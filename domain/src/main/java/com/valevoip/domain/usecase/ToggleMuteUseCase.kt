package com.valevoip.domain.usecase

import com.valevoip.domain.repository.ValeVoipService
import javax.inject.Inject

class ToggleMuteUseCase @Inject constructor(
    private val service: ValeVoipService
) {
    /**
     * Alterna o estado do microfone.
     * Se estiver ligado, desliga (muta). Se estiver desligado, liga (desmuta).
     */
    operator fun invoke(): Result<Unit> {
        return service.toggleMute()
    }
}