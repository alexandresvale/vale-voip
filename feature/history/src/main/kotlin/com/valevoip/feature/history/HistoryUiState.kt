package com.valevoip.feature.history

import com.valevoip.core.domain.model.CallHistoryItem

internal data class HistoryUiState(
    val isLoading: Boolean = false,
    val allItems: List<CallHistoryItem> = emptyList(),
    val filteredItems: List<CallHistoryItem> = emptyList(),
    val searchQuery: String = "",
    val filterMissed: Boolean = false,
    val errorMessage: String? = null,
    val selectedCallDetails: CallHistoryItem? = null
)
