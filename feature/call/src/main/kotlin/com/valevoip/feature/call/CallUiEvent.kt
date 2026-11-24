package com.valevoip.feature.call

internal sealed interface CallUiEvent {
    data object OnPermissionGranted : CallUiEvent
    data object OnToggleMute : CallUiEvent
    data object OnToggleSpeaker : CallUiEvent
    data object OnAudioRouteClick : CallUiEvent
    data object OnDismissAudioRouteSheet : CallUiEvent
    data class OnSelectAudioRoute(val routeId: String) : CallUiEvent
    data object OnShowKeypad : CallUiEvent
    data object OnDismissKeypad : CallUiEvent
    data class OnDtmfDigit(val digit: Char) : CallUiEvent
    data object OnToggleHold : CallUiEvent
    data object OnHangup : CallUiEvent
    data object OnAnswer : CallUiEvent
}
