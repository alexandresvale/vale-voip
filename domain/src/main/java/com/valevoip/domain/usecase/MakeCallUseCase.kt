package com.valevoip.domain.usecase

import com.valevoip.domain.repository.SipClient
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(number: String): Result<Unit> {
        return sipClient.makeCall(number)
    }
}