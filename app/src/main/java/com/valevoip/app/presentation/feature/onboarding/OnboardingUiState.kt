package com.valevoip.app.presentation.feature.onboarding

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userNameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val domain: String = "",
    val domainError: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val isConnectionError: Boolean = false
)
