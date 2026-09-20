package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(number: String): Result<Unit> {
        return sipClient.makeCall(number)
    }
}