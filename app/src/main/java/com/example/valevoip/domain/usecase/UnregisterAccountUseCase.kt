package com.example.valevoip.domain.usecase

import com.example.valevoip.data.SipAccountRepository
import kotlinx.coroutines.flow.Flow
import org.linphone.core.RegistrationState

class UnregisterAccountUseCase(
    private val repository: SipAccountRepository
) {
    suspend operator fun invoke(): Flow<RegistrationState> {
        return repository.unregisterAccount()
    }
}