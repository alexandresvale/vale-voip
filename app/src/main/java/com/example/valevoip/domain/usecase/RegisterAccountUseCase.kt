package com.example.valevoip.domain.usecase

import com.example.valevoip.data.SipAccountRepository
import kotlinx.coroutines.flow.Flow
import org.linphone.core.RegistrationState

class RegisterAccountUseCase(
    private val repository: SipAccountRepository
) {
    suspend operator fun invoke(userName: String, password: String, domain: String): Flow<RegistrationState> {
        return repository.registerAccount(userName, password, domain)
    }

}