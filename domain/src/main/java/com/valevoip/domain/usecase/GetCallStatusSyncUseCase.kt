package com.valevoip.domain.usecase

import com.valevoip.domain.model.CallStatus
import com.valevoip.domain.repository.SipClient
import javax.inject.Inject

class GetCallStatusSyncUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): CallStatus {
        return sipClient.getSynchronousCallStatus()
    }
}