package com.example.valevoip.domain.usecase

import com.example.valevoip.data.SipAccountRepository
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.Call

class GetCallStateUseCase(
    private val repository: SipAccountRepository
) {
    suspend operator fun invoke(): StateFlow<Call.State?> {
        return repository.getCallState()
    }
}