package com.valevoip.feature.login

import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipTransport

internal data class LoginUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userNameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val domain: String = "",
    val domainError: String? = null,
    val port: String = SipAccount.DEFAULT_SIP_PORT.toString(),
    val portError: String? = null,
    val transport: SipTransport = SipTransport.TCP,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val isConnectionError: Boolean = false
)
