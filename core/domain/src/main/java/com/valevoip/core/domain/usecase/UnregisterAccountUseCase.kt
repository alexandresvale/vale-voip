package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.repository.AccountRepository
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
    suspend operator fun invoke(): Flow<SipRegistrationState> {
        return sipClient.unregister().onEach {
            if (it == SipRegistrationState.Ok) {
                repository.clearAccount()
            }
        }.flowOn(dispatcher)
    }
}