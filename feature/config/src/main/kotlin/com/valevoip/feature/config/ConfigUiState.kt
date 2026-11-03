package com.valevoip.feature.config

import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.VoipSettings

data class ConfigUiState(
    val username: String = "",
    val domain: String = "",
    val registrationState: SipRegistrationState = SipRegistrationState.None,
    val settings: VoipSettings = VoipSettings(),
    val isIgnoringBatteryOptimizations: Boolean = true,
    val callHistoryCount: Int = 0,
    val showClearHistoryDialog: Boolean = false,
    val showLogoutDialog: Boolean = false
)
