package com.valevoip.domain.usecase

import com.valevoip.domain.repository.SipClient
import javax.inject.Inject

class HangUpUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): Result<Unit> {
        return sipClient.hangUp()
    }
}