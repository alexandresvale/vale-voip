package com.example.valevoip.domain.usecase

import com.example.valevoip.data.SipAccountRepository
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.RegistrationState

class GetRegistrationStateUseCase(
    private val repository: SipAccountRepository
) {
    suspend operator fun invoke(): StateFlow<RegistrationState?> {
        return repository.getRegistrationState()
    }
}