package com.valevoip.domain.usecase

import com.valevoip.domain.repository.SipClient
import javax.inject.Inject

class AnswerCallUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): Result<Unit> = sipClient.acceptCall()
}