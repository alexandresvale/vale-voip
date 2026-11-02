package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.repository.CallHistoryRepository
import javax.inject.Inject

class ClearCallHistoryUseCase @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return callHistoryRepository.clearCallHistory()
    }
}
