package com.valevoip.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.valevoip.app.VALEVOIP_TAG
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.GetAccountUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ObserveRegistrationStateUseCase
import com.valevoip.core.domain.usecase.RegisterAccountUseCase
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
internal class HomeViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val observeRegistrationStateUseCase: ObserveRegistrationStateUseCase,
    private val observeCallStateUseCase: ObserveCallStateUseCase,
    private val getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    init {
        monitorIncomingCalls()
        observeConnection()
        autoReconnect()
    }

    private fun observeConnection() {
        viewModelScope.launch {
            observeRegistrationStateUseCase().collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    private fun autoReconnect() {
        viewModelScope.launch {
            val account = getAccountUseCase()
            if (account != null) {
                registerAccountUseCase(account.username, account.password, account.domain)
            }
        }
    }

    private fun monitorIncomingCalls() {
        viewModelScope.launch {
            observeCallStateUseCase()
                .collect { status ->
                    Log.d("VALEVOIP_TAG", " HomeViewModel monitorIncomingCalls = $status")
                    if (status == CallStatus.INCOMING) {
                        val remoteNumber = getCurrentCallNumberUseCase()
                        Log.d("VALEVOIP_TAG", "HomeViewModel Recebendo chamada de: $remoteNumber")
                        _navigationChannel.send(remoteNumber)
                    }
                }
        }
    }
}
