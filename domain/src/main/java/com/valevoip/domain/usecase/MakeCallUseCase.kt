package com.valevoip.domain.usecase

import com.valevoip.domain.repository.ValeVoipService
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val valeVoipService: ValeVoipService
) {
    operator fun invoke(number: String): Result<Unit> {
        return valeVoipService.makeCall(number)
    }
}