package com.valevoip.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.usecase.RegisterAccountUseCase
import com.valevoip.core.domain.usecase.RegisterError
import com.valevoip.core.domain.usecase.RegisterResult
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
internal class LoginViewModel @Inject constructor(
    private val registerUserUseCase: RegisterAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnUsernameChange -> _uiState.update {
                it.copy(userName = event.value, userNameError = null)
            }

            is LoginUiEvent.OnPasswordChange -> _uiState.update {
                it.copy(password = event.value, passwordError = null)
            }

            is LoginUiEvent.OnDomainChange -> _uiState.update {
                it.copy(domain = event.value, domainError = null)
            }

            is LoginUiEvent.OnRegisterClick -> registerUser()
        }
    }

    private fun registerUser() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "", isConnectionError = false) }

            when (val result = registerUserUseCase(
                username = currentState.userName,
                password = currentState.password,
                domain = currentState.domain
            )) {
                is RegisterResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, userNameError = "", isConnectionError = false) }
                    _effect.send(LoginEffect.NavigateToDialer)
                }

                is RegisterResult.Error -> {
                    when (val error = result.error) {
                        is RegisterError.EmptyUsername -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                userNameError = "O Username é obrigatório",
                                isConnectionError = true
                            )
                        }

                        is RegisterError.EmptyPassword -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                passwordError = "A senha é obrigatória",
                                isConnectionError = true
                            )
                        }

                        is RegisterError.EmptyDomain -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                domainError = "O domínio é obrigatório",
                                isConnectionError = true
                            )
                        }

                        is RegisterError.SipClientError -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                statusMessage = error.cause.message ?: "Falha ao registrar",
                                isConnectionError = true
                            )
                        }

                        is RegisterError.InvalidDomain -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                domainError = "Formato inválido. Use um IP ou URL (ex: sip.info)",
                                isConnectionError = true
                            )
                        }
                    }
                }
            }
        }
    }
}