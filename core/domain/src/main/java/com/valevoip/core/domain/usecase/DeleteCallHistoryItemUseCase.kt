package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class DeleteCallHistoryItemUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(id: String): Result<Unit> {
        return sipClient.deleteCallLog(id)
    }
}
