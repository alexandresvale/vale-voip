package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.AudioRoute
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveAudioRoutesUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): StateFlow<List<AudioRoute>> = sipClient.availableAudioRoutes
}
