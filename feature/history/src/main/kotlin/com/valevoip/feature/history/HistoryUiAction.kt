package com.valevoip.feature.history

import com.valevoip.core.domain.model.CallHistoryItem

internal sealed interface HistoryUiAction {
    data object LoadHistory : HistoryUiAction
    data object ClearHistory : HistoryUiAction
    data class UpdateSearchQuery(val query: String) : HistoryUiAction
    data class UpdateFilterMissed(val missedOnly: Boolean) : HistoryUiAction
    data class DeleteHistoryItem(val item: CallHistoryItem) : HistoryUiAction
    data class CallContact(val number: String) : HistoryUiAction
    data class CopyNumber(val number: String) : HistoryUiAction
    data class ShowCallDetails(val item: CallHistoryItem) : HistoryUiAction
    data object DismissCallDetails : HistoryUiAction
}
