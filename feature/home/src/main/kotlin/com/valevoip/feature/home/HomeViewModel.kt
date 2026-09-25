package com.valevoip.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.usecase.GetAccountUseCase
import com.valevoip.core.domain.usecase.ObserveRegistrationStateUseCase
import com.valevoip.core.domain.usecase.RegisterAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val observeRegistrationStateUseCase: ObserveRegistrationStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
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
}
