package com.valevoip.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.usecase.ClearCallHistoryUseCase
import com.valevoip.core.domain.usecase.GetCallHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val clearCallHistoryUseCase: ClearCallHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadHistory() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            getCallHistoryUseCase().fold(
                onSuccess = { items ->
                    if (items.isEmpty()) {
                        _uiState.value = HistoryUiState.Empty
                    } else {
                        _uiState.value = HistoryUiState.Success(items)
                    }
                },
                onFailure = { error ->
                    _uiState.value = HistoryUiState.Error(error.message ?: "Erro desconhecido ao carregar histórico")
                }
            )
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            clearCallHistoryUseCase().onSuccess {
                loadHistory()
            }.onFailure { error ->
                _uiState.value = HistoryUiState.Error(error.message ?: "Erro ao limpar histórico")
            }
        }
    }
}