package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSpeakerStateUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): StateFlow<Boolean> = sipClient.isSpeakerEnabled
}
