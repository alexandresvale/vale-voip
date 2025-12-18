package com.example.valevoip.presentation.feature.onboarding

sealed class OnboardingEffect {
    data object NavigateToDialer : OnboardingEffect()
    data class ShowErrorSnackBar(val message: String) : OnboardingEffect()
}