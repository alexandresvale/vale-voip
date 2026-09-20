package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class AnswerCallUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): Result<Unit> = sipClient.acceptCall()
}