package com.example.valevoip.presentation.feature.onboarding

sealed interface OnboardingUiEvent {
    data class OnUsernameChange(val value: String) : OnboardingUiEvent
    data class OnPasswordChange(val value: String) : OnboardingUiEvent
    data class OnDomainChange(val value: String) : OnboardingUiEvent
    data object OnRegisterClick : OnboardingUiEvent
}