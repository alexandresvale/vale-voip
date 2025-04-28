package com.example.domain.usecase

import com.example.domain.repository.ValeVoipService

class MakeCallUseCase(private val voipService: ValeVoipService) {
    operator fun invoke(number: String): Result<Unit> {
        return voipService.makeCall(number)
    }
}