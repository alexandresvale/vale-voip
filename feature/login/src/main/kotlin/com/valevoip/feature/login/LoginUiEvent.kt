package com.valevoip.feature.login

internal sealed interface LoginUiEvent {
    data class OnUsernameChange(val value: String) : LoginUiEvent
    data class OnPasswordChange(val value: String) : LoginUiEvent
    data class OnDomainChange(val value: String) : LoginUiEvent
    data object OnRegisterClick : LoginUiEvent
}