package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.AudioRoute
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveActiveAudioRouteUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): StateFlow<AudioRoute?> = sipClient.activeAudioRoute
}
