package com.valevoip.feature.login

internal sealed class LoginEffect {
    data object NavigateToDialer : LoginEffect()
    data class ShowErrorSnackBar(val message: String) : LoginEffect()
}