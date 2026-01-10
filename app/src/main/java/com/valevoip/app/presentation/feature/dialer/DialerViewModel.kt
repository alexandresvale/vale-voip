package com.valevoip.app.presentation.feature.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DialerUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = Channel<DialerEffect>()
    val effects = _sideEffects.receiveAsFlow()

    fun onEvent(event: DialerUiEvent) {
        when (event) {
            is DialerUiEvent.OnDigitClick -> onDigitClick(event.digit)
            DialerUiEvent.OnBackspace -> onBackspace()
            DialerUiEvent.OnLongClickZero -> onLongClickZero()
            DialerUiEvent.OnCallClick -> onCallClick()
        }
    }

    private fun onDigitClick(digit: String) {
        if (_uiState.value.number.length < 30) {
            _uiState.value = _uiState.value.copy(number = _uiState.value.number + digit)
        }
    }

    private fun onBackspace() {
        val currentNumber = _uiState.value.number
        if (currentNumber.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(number = currentNumber.dropLast(1))
        }
    }

    private fun onLongClickZero() {
        onDigitClick("+")
    }

    private fun onTextChange(newText: String) {
        // Usado quando o teclado QWERTY está ativo
        _uiState.value = _uiState.value.copy(number = newText)
    }

    fun toggleInputMode() {
        _uiState.value = _uiState.value.copy(
            isAlphaNumericMode = !_uiState.value.isAlphaNumericMode
        )
    }

    private fun onCallClick() {
        val number = _uiState.value.number
        if (number.isNotBlank()) {
            // 2. Dispara o efeito de navegação
            viewModelScope.launch {
                _sideEffects.send(DialerEffect.NavigateToCall(number))
            }
            // Aqui você também iniciaria a preparação do serviço SIP
        } else {
            // Opcional: Mostrar erro
            viewModelScope.launch {
                _sideEffects.send(DialerEffect.ShowError("Digite um número válido"))
            }
        }
    }
}