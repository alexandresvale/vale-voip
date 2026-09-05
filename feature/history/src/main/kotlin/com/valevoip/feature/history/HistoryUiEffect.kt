package com.valevoip.feature.history

internal sealed interface HistoryUiEffect {
    data class NavigateToCall(val number: String) : HistoryUiEffect
    data class ShowToast(val message: String) : HistoryUiEffect
    data class CopyToClipboard(val number: String) : HistoryUiEffect
}
