package com.valevoip.domain.usecase

import com.valevoip.domain.di.IoDispatcher
import com.valevoip.domain.model.AccountModel
import com.valevoip.domain.model.RegistrationStatus
import com.valevoip.domain.repository.AccountRepository
import com.valevoip.domain.repository.SipClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val sipClient: SipClient,
    private val repository: AccountRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(userName: String, password: String, domain: String): Flow<RegistrationStatus> {
        return sipClient.registerUser(userName, password, domain).onEach { status ->
            if (status == RegistrationStatus.Ok) {
                repository.insertAccount(
                    accountModel = AccountModel(
                        username = userName,
                        password = password,
                        serverDomain = domain
                    )
                )
            }
        }.flowOn(dispatcher)
    }
}