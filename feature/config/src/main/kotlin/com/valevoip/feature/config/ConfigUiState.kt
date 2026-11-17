package com.valevoip.feature.config

import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.SipTransport
import com.valevoip.core.domain.model.VoipSettings

data class ConfigUiState(
    val username: String = "",
    val domain: String = "",
    val port: Int = SipAccount.DEFAULT_SIP_PORT,
    val transport: SipTransport = SipTransport.TCP,
    val registrationState: SipRegistrationState = SipRegistrationState.None,
    val settings: VoipSettings = VoipSettings(),
    val isIgnoringBatteryOptimizations: Boolean = true,
    val callHistoryCount: Int = 0,
    val showClearHistoryDialog: Boolean = false,
    val showLogoutDialog: Boolean = false
)
