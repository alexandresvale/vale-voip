package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class ToggleMuteUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    /**
     * Alterna o estado do microfone.
     * Se estiver ligado, desliga (muta). Se estiver desligado, liga (desmuta).
     */
    operator fun invoke(): Result<Unit> {
        return sipClient.toggleMute()
    }
}