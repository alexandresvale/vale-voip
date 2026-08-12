package com.valevoip.feature.history

import com.valevoip.core.domain.model.CallHistoryItem

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Success(val items: List<CallHistoryItem?>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}