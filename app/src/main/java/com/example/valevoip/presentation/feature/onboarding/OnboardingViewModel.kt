package com.example.valevoip.presentation.feature.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.RegistrationStatus
import com.example.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OnboardingEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.OnUsernameChange -> {
                _uiState.update { it.copy(userName = event.value, userNameError = null) }
            }

            is OnboardingUiEvent.OnPasswordChange -> {
                _uiState.update { it.copy(password = event.value, passwordError = null) }
            }

            is OnboardingUiEvent.OnDomainChange -> {
                _uiState.update { it.copy(domain = event.value, domainError = null) }
            }

            is OnboardingUiEvent.OnRegisterClick -> onRegisterButtonClick()
        }
    }

    fun onRegisterButtonClick() {
        if (validateInputs()) {
            Log.d("VALEVOIP", "Formulário Válido! Iniciando registro...")
            registerUser()
        } else {
            Log.d("VALEVOIP", "Formulário Inválido. Verifique os campos.")
        }
    }

    private fun registerUser() {
        val currentState = _uiState.value
        viewModelScope.launch {
            registerUserUseCase(
                userName = currentState.userName,
                password = currentState.password,
                domain = currentState.domain
            ).onStart {
                Log.d("VALEVOIP", "OnboardingViewModel onRegisterButtonClick onStart Iniciando...")
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        statusMessage = "Iniciando conexão...",
                        isConnectionError = false
                    )
                }
            }.onCompletion { cause ->
                if (cause == null) {
                    Log.d("VALEVOIP", "Fluxo completado")
                }
                _uiState.update { it.copy(isLoading = false) }
                Log.d("VALEVOIP", "OnboardingViewModel onRegisterButtonClick onCompletion = $cause")
            }.catch { error ->
                Log.d("VALEVOIP", "OnboardingViewModel onRegisterButtonClick catch = $error")
                Log.d("VALEVOIP", "Catch Erro: ${error.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Erro: ${error.message ?: "Falha desconhecida"}",
                        isConnectionError = true
                    )
                }
                _effect.send(OnboardingEffect.ShowErrorSnackBar(error.message ?: "Erro desconhecido"))
            }.collect { status ->
                Log.d("VALEVOIP", "OnboardingViewModel onRegisterButtonClick collect = $status")
                when (status) {
                    RegistrationStatus.Progress, RegistrationStatus.Refreshing -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                statusMessage = status.toUserMessage(),
                                isConnectionError = false
                            )
                        }
                    }

                    RegistrationStatus.Ok -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                statusMessage = status.toUserMessage(),
                                isConnectionError = false
                            )
                        }
                        _effect.send(OnboardingEffect.NavigateToDialer)
                    }

                    RegistrationStatus.Failed -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                statusMessage = status.toUserMessage(),
                                isConnectionError = true
                            )
                        }
                    }

                    RegistrationStatus.Cleared, RegistrationStatus.None -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                statusMessage = status.toUserMessage(),
                                isConnectionError = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        val userError = if (currentState.userName.isBlank()) {
            isValid = false
            "O nome de usuário é obrigatório"
        } else {
            null
        }

        val passError = if (currentState.password.isBlank()) {
            isValid = false
            "A senha não pode estar vazia"
        } else {
            null
        }

        val portError = if (currentState.domain.isBlank()) {
            isValid = false
            "Informe a porta"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                userNameError = userError,
                passwordError = passError,
                domainError = portError
            )
        }

        return isValid
    }
}