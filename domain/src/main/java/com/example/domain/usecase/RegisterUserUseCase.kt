package com.example.domain.usecase

import com.example.domain.model.RegistrationStatus
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class RegisterUserUseCase(
    private val voipService: ValeVoipService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(userName: String, password: String, domain: String): Flow<RegistrationStatus> {
        return voipService.registerUser(userName, password, domain).onEach { status ->
            if (status == RegistrationStatus.Ok) {
                println("Igual a ok")
            } else {
                println("Diferente a ok")
            }
        }.flowOn(dispatcher)
    }
}