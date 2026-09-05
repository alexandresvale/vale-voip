package com.valevoip.feature.dialer

internal sealed interface DialerUiEvent {
    data class OnDigitClick(val digit: String) : DialerUiEvent
    object OnBackspace : DialerUiEvent
    object OnLongClickZero : DialerUiEvent
    object OnCallClick : DialerUiEvent
}
