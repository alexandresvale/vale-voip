package com.example.valevoip.presentation.feature.call

sealed interface CallUiEvent {
    object OnToggleMute : CallUiEvent
    object OnToggleSpeaker : CallUiEvent
    object OnShowKeypad : CallUiEvent
    object OnHangup : CallUiEvent
    object OnAnswer : CallUiEvent
}