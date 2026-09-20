package com.valevoip.feature.login

internal data class LoginUiState(
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
