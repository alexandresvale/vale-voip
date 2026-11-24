package com.valevoip.feature.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DialerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DialerUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = Channel<DialerEffect>()
    val effects = _sideEffects.receiveAsFlow()

    fun onEvent(event: DialerUiEvent) {
        when (event) {
            is DialerUiEvent.OnDigitClick -> onDigitClick(event.digit)
            is DialerUiEvent.OnTextChange -> onTextChange(event.text)
            is DialerUiEvent.OnToggleInputMode -> toggleInputMode()
            DialerUiEvent.OnActivateAlphaNumericMode -> activateAlphaNumericMode()
            DialerUiEvent.OnBackspace -> onBackspace()
            DialerUiEvent.OnClearAll -> onClearAll()
            DialerUiEvent.OnLongClickZero -> onLongClickZero()
            DialerUiEvent.OnCallClick -> onCallClick()
        }
    }

    private fun onDigitClick(digit: String) {
        _uiState.update { current ->
            if (current.number.length < MAX_TARGET_LENGTH) {
                current.copy(number = current.number + digit)
            } else {
                current
            }
        }
    }

    private fun onBackspace() {
        _uiState.update { current ->
            if (current.number.isNotEmpty()) {
                current.copy(number = current.number.dropLast(1))
            } else {
                current
            }
        }
    }

    private fun onClearAll() {
        _uiState.update { it.copy(number = "") }
    }

    private fun onLongClickZero() {
        onDigitClick("+")
    }

    private fun onTextChange(newText: String) {
        val sanitized = newText
            .replace("\n", "")
            .replace("\r", "")
            .take(MAX_TARGET_LENGTH)
        _uiState.update { it.copy(number = sanitized) }
    }

    fun toggleInputMode() {
        _uiState.update { it.copy(isAlphaNumericMode = !it.isAlphaNumericMode) }
    }

    private fun activateAlphaNumericMode() {
        _uiState.update { current ->
            if (current.isAlphaNumericMode) current else current.copy(isAlphaNumericMode = true)
        }
    }

    private fun onCallClick() {
        val target = _uiState.value.number.trim()
        if (target.isNotEmpty()) {
            viewModelScope.launch {
                _sideEffects.send(DialerEffect.NavigateToCall(target))
            }
        } else {
            viewModelScope.launch {
                _sideEffects.send(DialerEffect.ShowError("Digite um ramal ou usuário SIP válido"))
            }
        }
    }

    private companion object {
        const val MAX_TARGET_LENGTH = 128
    }
}
