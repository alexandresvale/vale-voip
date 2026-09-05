package com.valevoip.core.domain.model

sealed interface SipRegistrationState {
    data object None : SipRegistrationState
    data object Progress : SipRegistrationState
    data object Ok : SipRegistrationState
    data object Cleared : SipRegistrationState
    data class Failed(val reason: String) : SipRegistrationState
    data object Refreshing : SipRegistrationState
}