package com.example.domain.usecase

import com.example.domain.repository.ValeVoipService
import javax.inject.Inject

class AnswerCallUseCase @Inject constructor(
    private val valeVoipService: ValeVoipService
) {
    operator fun invoke(): Result<Unit> = valeVoipService.acceptCall()
}