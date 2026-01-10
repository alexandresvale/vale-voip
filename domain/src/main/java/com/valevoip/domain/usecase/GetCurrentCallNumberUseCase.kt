package com.valevoip.domain.usecase

import com.valevoip.domain.repository.SipClient
import javax.inject.Inject

class GetCurrentCallNumberUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): String {
        return sipClient.getCurrentCallNumber() ?: "Desconhecido"
    }
}