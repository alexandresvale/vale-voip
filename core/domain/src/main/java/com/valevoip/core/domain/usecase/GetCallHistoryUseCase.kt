package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val sipClient: SipClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Result<List<CallHistoryItem>> {
        return withContext(ioDispatcher) {
            sipClient.getCallLogs()
        }
    }
}
