package com.example.domain.usecase

import com.example.domain.repository.ValeVoipService
import javax.inject.Inject

class HangUpUseCase @Inject constructor(
    private val service: ValeVoipService
) {
    operator fun invoke(): Result<Unit> {
        return service.hangUp()
    }
}