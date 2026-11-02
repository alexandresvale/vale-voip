package com.valevoip.feature.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.usecase.ClearCallHistoryUseCase
import com.valevoip.core.domain.usecase.GenerateDiagnosticReportUseCase
import com.valevoip.core.domain.usecase.GetAccountUseCase
import com.valevoip.core.domain.usecase.GetCallHistoryUseCase
import com.valevoip.core.domain.usecase.ObserveRegistrationStateUseCase
import com.valevoip.core.domain.usecase.ObserveVoipSettingsUseCase
import com.valevoip.core.domain.usecase.SetEchoCancellationUseCase
import com.valevoip.core.domain.usecase.SetVibrateOnCallUseCase
import com.valevoip.core.domain.usecase.SetWifiOnlyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val observeRegistrationStateUseCase: ObserveRegistrationStateUseCase,
    private val observeVoipSettingsUseCase: ObserveVoipSettingsUseCase,
    private val setEchoCancellationUseCase: SetEchoCancellationUseCase,
    private val setVibrateOnCallUseCase: SetVibrateOnCallUseCase,
    private val setWifiOnlyUseCase: SetWifiOnlyUseCase,
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val clearCallHistoryUseCase: ClearCallHistoryUseCase,
    private val generateDiagnosticReportUseCase: GenerateDiagnosticReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ConfigUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadAccountInfo()
        observeRegistration()
        observeSettings()
        loadHistoryCount()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            val account = getAccountUseCase()
            if (account != null) {
                _uiState.update {
                    it.copy(
                        username = account.username,
                        domain = account.domain
                    )
                }
            }
        }
    }

    private fun observeRegistration() {
        observeRegistrationStateUseCase()
            .onEach { state ->
                _uiState.update { it.copy(registrationState = state) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        observeVoipSettingsUseCase()
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    fun loadHistoryCount() {
        viewModelScope.launch {
            getCallHistoryUseCase().fold(
                onSuccess = { logs ->
                    _uiState.update { it.copy(callHistoryCount = logs.size) }
                },
                onFailure = {
                    _uiState.update { it.copy(callHistoryCount = 0) }
                }
            )
        }
    }

    fun onAction(action: ConfigUiAction) {
        when (action) {
            is ConfigUiAction.SetEchoCancellation -> {
                viewModelScope.launch {
                    setEchoCancellationUseCase(action.enabled)
                }
            }
            is ConfigUiAction.SetVibrate -> {
                viewModelScope.launch {
                    setVibrateOnCallUseCase(action.enabled)
                }
            }
            is ConfigUiAction.SetWifiOnly -> {
                viewModelScope.launch {
                    setWifiOnlyUseCase(action.enabled)
                }
            }
            is ConfigUiAction.OpenClearHistoryDialog -> {
                _uiState.update { it.copy(showClearHistoryDialog = true) }
            }
            is ConfigUiAction.DismissClearHistoryDialog -> {
                _uiState.update { it.copy(showClearHistoryDialog = false) }
            }
            is ConfigUiAction.ConfirmClearHistory -> {
                _uiState.update { it.copy(showClearHistoryDialog = false) }
                viewModelScope.launch {
                    clearCallHistoryUseCase().fold(
                        onSuccess = {
                            _uiState.update { it.copy(callHistoryCount = 0) }
                            _uiEffect.emit(ConfigUiEffect.ShowToast("Histórico de chamadas limpo"))
                        },
                        onFailure = { error ->
                            _uiEffect.emit(ConfigUiEffect.ShowToast("Erro ao limpar: ${error.message}"))
                        }
                    )
                }
            }
            is ConfigUiAction.ShareDiagnosticReport -> {
                viewModelScope.launch {
                    val report = generateDiagnosticReportUseCase()
                    _uiEffect.emit(ConfigUiEffect.ShareText(report))
                }
            }
        }
    }
}
