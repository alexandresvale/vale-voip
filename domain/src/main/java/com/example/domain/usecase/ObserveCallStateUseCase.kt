package com.example.domain.usecase

import com.example.domain.model.CallStatus
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ObserveCallStateUseCase @Inject constructor(
    private val service: ValeVoipService
) {
    operator fun invoke(): Flow<CallStatus> {
        return service.getCallStatusFlow().distinctUntilChanged()
    }
}