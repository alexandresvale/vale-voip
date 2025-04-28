package com.example.domain.usecase

import com.example.domain.model.AccountModel
import com.example.domain.model.RegistrationStatus
import com.example.domain.repository.AccountRepository
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class RegisterUserUseCase(
    private val valeVoipService: ValeVoipService,
    private val repository: AccountRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(userName: String, password: String, domain: String): Flow<RegistrationStatus> {
        return valeVoipService.registerUser(userName, password, domain).onEach { status ->
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