package com.example.valevoip.presentation.feature.dialer

sealed interface DialerEffect {
    data class NavigateToCall(val number: String) : DialerEffect
    data class ShowError(val message: String) : DialerEffect
}