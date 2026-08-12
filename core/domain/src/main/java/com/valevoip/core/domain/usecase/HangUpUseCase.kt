package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class HangUpUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): Result<Unit> {
        return sipClient.hangUp()
    }
}