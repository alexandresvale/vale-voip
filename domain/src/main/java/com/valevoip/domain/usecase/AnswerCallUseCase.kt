package com.valevoip.domain.usecase

import com.valevoip.domain.repository.ValeVoipService
import javax.inject.Inject

class AnswerCallUseCase @Inject constructor(
    private val valeVoipService: ValeVoipService
) {
    operator fun invoke(): Result<Unit> = valeVoipService.acceptCall()
}