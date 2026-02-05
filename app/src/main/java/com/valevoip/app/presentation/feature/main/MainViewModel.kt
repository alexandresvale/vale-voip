package com.valevoip.app.presentation.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.app.VALEVOIP_TAG
import com.valevoip.domain.model.CallStatus
import com.valevoip.domain.usecase.GetAccountUseCase
import com.valevoip.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.domain.usecase.ObserveCallStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val observeCallStateUseCase: ObserveCallStateUseCase,
    private val getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    init {
        monitorIncomingCalls()
    }

    fun checkUseConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val configModel = getAccountUseCase()
            _uiState.update { currentState ->
                if (configModel != null) {
                    Log.d("####", "ConfigModel não é nulo = $configModel")
                    currentState.copy(hasUserConfig = true)
                } else {
                    Log.d("####", "ConfigModel é nulo = $configModel")
                    currentState.copy(hasUserConfig = false)
                }
            }
        }
    }

    private fun monitorIncomingCalls() {
        viewModelScope.launch {
            observeCallStateUseCase()
                .collect { status ->
                    Log.d(VALEVOIP_TAG, " MainViewModel monitorIncomingCalls = $status")
                    if (status == CallStatus.INCOMING) {
                        val remoteNumber = getCurrentCallNumberUseCase()
                        Log.d(VALEVOIP_TAG, " MainViewModel Recebendo chamada de: $remoteNumber")
                        _navigationChannel.send(remoteNumber)
                    }
                }
        }
    }

    fun handleNotificationClick(number: String) {
        viewModelScope.launch {
            _navigationChannel.send(number)
        }
    }
}