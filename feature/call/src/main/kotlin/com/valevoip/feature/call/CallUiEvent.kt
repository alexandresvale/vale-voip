package com.valevoip.feature.call

internal sealed interface CallUiEvent {
    object OnPermissionGranted : CallUiEvent
    object OnToggleMute : CallUiEvent
    object OnToggleSpeaker : CallUiEvent
    object OnShowKeypad : CallUiEvent
    object OnHangup : CallUiEvent
    object OnAnswer : CallUiEvent
}