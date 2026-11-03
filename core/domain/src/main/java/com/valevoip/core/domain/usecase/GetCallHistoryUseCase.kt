package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.repository.CallHistoryRepository
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository
) {
    suspend operator fun invoke(): Result<List<CallHistoryItem>> {
        return callHistoryRepository.getCallHistory()
    }
}
