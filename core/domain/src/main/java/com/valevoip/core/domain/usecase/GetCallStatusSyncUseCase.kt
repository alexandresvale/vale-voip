package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.client.SipClient
import javax.inject.Inject

class GetCallStatusSyncUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): CallStatus {
        return sipClient.getSynchronousCallStatus()
    }
}