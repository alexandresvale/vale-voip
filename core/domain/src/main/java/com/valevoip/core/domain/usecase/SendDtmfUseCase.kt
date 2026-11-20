package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class SendDtmfUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(digit: Char): Result<Unit> {
        val normalizedDigit = digit.uppercaseChar()
        if (normalizedDigit !in ALLOWED_DTMF_DIGITS) {
            return Result.failure(
                IllegalArgumentException("Dígito DTMF inválido: '$digit'.")
            )
        }
        return sipClient.sendDtmf(normalizedDigit)
    }

    private companion object {
        val ALLOWED_DTMF_DIGITS = setOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '*', '#', 'A', 'B', 'C', 'D'
        )
    }
}
