package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.client.SipClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ObserveCallStateUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): Flow<CallStatus> {
        return sipClient.getCallStatusFlow().distinctUntilChanged()
    }
}