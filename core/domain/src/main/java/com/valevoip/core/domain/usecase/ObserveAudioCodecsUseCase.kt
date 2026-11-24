package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.AudioCodec
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveAudioCodecsUseCase @Inject constructor(
    private val sipClient: SipClient
) {
    operator fun invoke(): StateFlow<List<AudioCodec>> = sipClient.availableAudioCodecs
}
