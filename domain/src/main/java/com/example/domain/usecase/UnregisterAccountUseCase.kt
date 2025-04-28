package com.example.domain.usecase

import com.example.domain.model.RegistrationStatus
import com.example.domain.repository.AccountRepository
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class UnregisterAccountUseCase(
    private val valeVoipService: ValeVoipService,
    private val repository: AccountRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Flow<RegistrationStatus> {
        return valeVoipService.unregister().onEach {
            if (it == RegistrationStatus.Cleared) {
                repository.clearAccount()
            }
        }.flowOn(dispatcher)
    }
}