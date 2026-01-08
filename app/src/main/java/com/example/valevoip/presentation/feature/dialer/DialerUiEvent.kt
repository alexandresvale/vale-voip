package com.example.valevoip.presentation.feature.dialer

sealed interface DialerUiEvent {
    data class OnDigitClick(val digit: String) : DialerUiEvent
    object OnBackspace : DialerUiEvent
    object OnLongClickZero : DialerUiEvent
    object OnCallClick : DialerUiEvent
}