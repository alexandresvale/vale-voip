package com.valevoip.feature.login

import com.valevoip.core.domain.model.SipTransport

internal sealed interface LoginUiEvent {
    data class OnUsernameChange(val value: String) : LoginUiEvent
    data class OnPasswordChange(val value: String) : LoginUiEvent
    data class OnDomainChange(val value: String) : LoginUiEvent
    data class OnTransportChange(val value: SipTransport) : LoginUiEvent
    data object OnRegisterClick : LoginUiEvent
}