package com.valevoip.feature.config

import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.VoipSettings

data class ConfigUiState(
    val username: String = "",
    val domain: String = "",
    val registrationState: SipRegistrationState = SipRegistrationState.None,
    val settings: VoipSettings = VoipSettings(),
    val callHistoryCount: Int = 0,
    val isTestingAudio: Boolean = false,
    val showClearHistoryDialog: Boolean = false,
    val showAudioTestBottomSheet: Boolean = false
)
