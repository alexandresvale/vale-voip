package com.example.valevoip.feature.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetAccountUseCase
import com.example.domain.usecase.RegisterUserUseCase
import com.example.domain.usecase.UnregisterAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
    private val unregisterAccountUseCase: UnregisterAccountUseCase,
    private val getAccountUseCase: GetAccountUseCase
//    private val getRegistrationStateUseCase: GetRegistrationStateUseCase,
//    private val acceptUseCase: AcceptUseCase,
//    private val getCallStateUseCase: GetCallStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugState())
    val uiState: StateFlow<DebugState> = _uiState.asStateFlow()

    init {
        getRegistrationState()
        getCallState()
    }

    fun onAction(action: DebugAction) {
        logEvent("onAction $action")
        when (action) {
            is DebugAction.RegisterUser -> registerUser()
            is DebugAction.FetchUser -> checkUseConfig()
            is DebugAction.Unregister -> unregisterAccount()
            is DebugAction.IncomingCall -> acceptCall()
            is DebugAction.Retry -> {}
            is DebugAction.Call -> call()
        }
    }

    private fun registerUser() {
        viewModelScope.launch {
            registerUserUseCase(userName = "5002", password = "5002", "192.168.100.12")
                .onStart {
                    setState { it.setLoadingState(isLoading = true) }
                    logEvent("onStart")
                }
                .onCompletion {
                    setState { it.setLoadingState(isLoading = false) }
                    logEvent("onCompletion - $it")
                }
                .catch {
                    logEvent("Error = $it")
                }
                .collect {
                    logEvent("Sucesso = $it")
                }
            logEvent("Thread atual launch: ${Thread.currentThread().name}")
        }
        logEvent("Thread atual: ${Thread.currentThread().name}")
    }

    private fun unregisterAccount() {
        viewModelScope.launch {
            unregisterAccountUseCase()
                .onStart { setState { it.setLoadingState(isLoading = true) } }
                .onCompletion { setState { it.setLoadingState(isLoading = false) } }
                .catch { throwable ->
                    logEvent("unregisterAccount catch $throwable")
                    setState { it.setErrorState(errorMessage = throwable.message ?: "fail") }
                }.collect { registerState ->
                    logEvent("registerAccount flow collect = $registerState")
                    setState { it.setSuccessState(message = registerState.name) }
                }
        }
    }

    private fun checkUseConfig() {
        viewModelScope.launch {
            logEvent("registerAccount flow collect")
            val configModel = getAccountUseCase()
            if (configModel != null) {
                logEvent("ConfigModel não é nulo = $configModel")
            } else {
                logEvent("ConfigModel é nulo = $configModel")
            }
        }
    }

    private fun acceptCall() {
        /*viewModelScope.launch {
            acceptUseCase()
        }*/
    }

    private fun call() {
        var callId = "5001"
    }

    private fun getRegistrationState() {
        /*viewModelScope.launch {
            getRegistrationStateUseCase()
                .onStart { logEvent("RegistrationStateUseCase onStart") }
                .onCompletion { logEvent("RegistrationStateUseCase onCompletion") }
                .catch { logEvent("RegistrationStateUseCase catch $it") }
                .collect {
                    logEvent("RegistrationStateUseCase collect = $it")
                }
        }*/
    }

    private fun getCallState() {
        /*viewModelScope.launch {
            getCallStateUseCase()
                .onStart { logEvent("CallState onStart") }
                .onCompletion { logEvent("CallState onCompletion") }
                .catch { logEvent("CallState catch = $it") }
                .collect {
                    logEvent("CallState collect = $it")
                }
        }*/
    }


    private fun setState(update: (DebugState) -> DebugState) {
        _uiState.update { currentState ->
            update(currentState)
        }
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "DebugViewModel | $string")
    }

}