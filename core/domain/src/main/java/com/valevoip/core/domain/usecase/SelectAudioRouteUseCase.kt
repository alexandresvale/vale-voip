package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class SelectAudioRouteUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(routeId: String): Result<Unit> {
        if (routeId.isBlank()) {
            return Result.failure(IllegalArgumentException("Identificador da rota de áudio inválido."))
        }
        return sipClient.selectAudioRoute(routeId)
    }
}
