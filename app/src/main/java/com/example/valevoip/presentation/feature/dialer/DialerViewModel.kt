package com.example.valevoip.presentation.feature.dialer

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DialerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DialerUiState())
    val uiState = _uiState.asStateFlow()

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
        val numberToCall = _uiState.value.number
        if (numberToCall.isNotBlank()) {
            Log.d("Dialer", "Chamando: $numberToCall")
        }
    }
}