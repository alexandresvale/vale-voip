package com.valevoip.domain.usecase

import com.valevoip.domain.repository.ValeVoipService
import javax.inject.Inject

class GetCurrentCallNumberUseCase @Inject constructor(
    private val service: ValeVoipService
) {
    operator fun invoke(): String {
        return service.getCurrentCallNumber() ?: "Desconhecido"
    }
}