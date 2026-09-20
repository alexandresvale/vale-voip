package com.valevoip.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import com.valevoip.core.domain.usecase.ClearCallHistoryUseCase
import com.valevoip.core.domain.usecase.DeleteCallHistoryItemUseCase
import com.valevoip.core.domain.usecase.GetCallHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val clearCallHistoryUseCase: ClearCallHistoryUseCase,
    private val deleteCallHistoryItemUseCase: DeleteCallHistoryItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HistoryUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.LoadHistory -> loadHistory()
            is HistoryUiAction.ClearHistory -> clearHistory()
            is HistoryUiAction.UpdateSearchQuery -> updateSearchQuery(action.query)
            is HistoryUiAction.UpdateFilterMissed -> updateFilterMissed(action.missedOnly)
            is HistoryUiAction.DeleteHistoryItem -> deleteHistoryItem(action.item)
            is HistoryUiAction.CallContact -> viewModelScope.launch { _uiEffect.emit(HistoryUiEffect.NavigateToCall(action.number)) }
            is HistoryUiAction.CopyNumber -> viewModelScope.launch { 
                _uiEffect.emit(HistoryUiEffect.CopyToClipboard(action.number))
                _uiEffect.emit(HistoryUiEffect.ShowToast("Número copiado"))
            }
            is HistoryUiAction.ShowCallDetails -> _uiState.update { it.copy(selectedCallDetails = action.item) }
            is HistoryUiAction.DismissCallDetails -> _uiState.update { it.copy(selectedCallDetails = null) }
        }
    }

    private fun loadHistory() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getCallHistoryUseCase().fold(
                onSuccess = { items ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allItems = items,
                            filteredItems = applyFilters(items, state.searchQuery, state.filterMissed)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro desconhecido ao carregar histórico"
                        )
                    }
                }
            )
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            clearCallHistoryUseCase().onSuccess {
                loadHistory()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Erro ao limpar histórico") }
            }
        }
    }

    private fun deleteHistoryItem(item: CallHistoryItem) {
        viewModelScope.launch {
            deleteCallHistoryItemUseCase(item.id).onSuccess {
                loadHistory()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Erro ao apagar histórico") }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredItems = applyFilters(state.allItems, query, state.filterMissed)
            )
        }
    }

    private fun updateFilterMissed(missedOnly: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterMissed = missedOnly,
                filteredItems = applyFilters(state.allItems, state.searchQuery, missedOnly)
            )
        }
    }

    private fun applyFilters(
        items: List<CallHistoryItem>,
        query: String,
        missedOnly: Boolean
    ): List<CallHistoryItem> {
        return items.filter { item ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.lowercase()
                item.remoteAddress.lowercase().contains(q) || (item.displayName?.lowercase()?.contains(q) == true)
            }

            val matchesMissed = if (missedOnly) {
                item.status == CallHistoryStatus.MISSED
            } else {
                true
            }

            matchesQuery && matchesMissed
        }
    }
}
