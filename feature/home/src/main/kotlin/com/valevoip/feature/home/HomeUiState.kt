package com.valevoip.feature.home

import com.valevoip.core.domain.model.SipRegistrationState

internal data class HomeUiState(
    val isLoading: Boolean = false,
    val hasUserConfig: Boolean = false,
    val connectionState: SipRegistrationState = SipRegistrationState.None
)
