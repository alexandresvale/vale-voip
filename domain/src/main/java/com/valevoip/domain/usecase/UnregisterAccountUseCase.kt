package com.valevoip.domain.usecase

import com.valevoip.domain.di.IoDispatcher
import com.valevoip.domain.model.RegistrationStatus
import com.valevoip.domain.repository.AccountRepository
import com.valevoip.domain.repository.SipClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class UnregisterAccountUseCase @Inject constructor(
    private val sipClient: SipClient,
    private val repository: AccountRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Flow<RegistrationStatus> {
        return sipClient.unregister().onEach {
            if (it == RegistrationStatus.Cleared) {
                repository.clearAccount()
            }
        }.flowOn(dispatcher)
    }
}