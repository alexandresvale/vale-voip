package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class GetCurrentCallNumberUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): String {
        return sipClient.getCurrentCallNumber() ?: "Desconhecido"
    }
}