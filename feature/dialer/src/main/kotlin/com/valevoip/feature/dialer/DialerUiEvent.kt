package com.valevoip.feature.dialer

internal sealed interface DialerUiEvent {
    data class OnDigitClick(val digit: String) : DialerUiEvent
    data class OnTextChange(val text: String) : DialerUiEvent
    data object OnToggleInputMode : DialerUiEvent
    data object OnActivateAlphaNumericMode : DialerUiEvent
    data object OnBackspace : DialerUiEvent
    data object OnClearAll : DialerUiEvent
    data object OnLongClickZero : DialerUiEvent
    data object OnCallClick : DialerUiEvent
}
