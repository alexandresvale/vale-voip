package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.repository.CallHistoryRepository
import javax.inject.Inject

class DeleteCallHistoryItemUseCase @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return callHistoryRepository.deleteCallHistoryItem(id)
    }
}
