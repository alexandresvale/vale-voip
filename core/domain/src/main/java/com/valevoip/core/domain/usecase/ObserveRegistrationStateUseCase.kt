package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.SipRegistrationState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveRegistrationStateUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): StateFlow<SipRegistrationState> = sipClient.registrationState
}