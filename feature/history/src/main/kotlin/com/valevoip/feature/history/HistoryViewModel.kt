package com.valevoip.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import com.valevoip.core.domain.usecase.ClearCallHistoryUseCase
import com.valevoip.core.domain.usecase.GetCallHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val clearCallHistoryUseCase: ClearCallHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun loadHistory() {
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

    fun clearHistory() {
        viewModelScope.launch {
            clearCallHistoryUseCase().onSuccess {
                loadHistory()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Erro ao limpar histórico") }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredItems = applyFilters(state.allItems, query, state.filterMissed)
            )
        }
    }

    fun updateFilterMissed(missedOnly: Boolean) {
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