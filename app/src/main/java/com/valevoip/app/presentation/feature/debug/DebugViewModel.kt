package com.valevoip.app.presentation.feature.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.domain.usecase.AnswerCallUseCase
import com.valevoip.domain.usecase.GetAccountUseCase
import com.valevoip.domain.usecase.MakeCallUseCase
import com.valevoip.domain.usecase.RegisterUserUseCase
import com.valevoip.domain.usecase.UnregisterAccountUseCase
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
    private val getAccountUseCase: GetAccountUseCase,
    private val makeCallUseCase: MakeCallUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
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
            registerUserUseCase(userName = "alexandresvale", password = "asv#251091", "sip2sip.info")
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
        viewModelScope.launch {
            answerCallUseCase()
        }
    }

    private fun call() {
        // O UseCase retorna Result<Unit> imediatamente (síncrono)
        makeCallUseCase("valevoipios")
            .onSuccess {
                // O pedido foi aceito pelo Linphone.
                // IMPORTANTE: Ainda não mudamos para ACTIVE/CONNECTED aqui.
                // O estado deve mudar apenas quando o Listener do Linphone disser "Connected".
                // Por enquanto, continuamos em DIALING.
                Log.d("CallViewModel", "Chamada enviada com sucesso para o core.")
            }
            .onFailure { error ->
                // Falha imediata (ex: sem internet, sem conta)
                Log.e("CallViewModel", "Erro ao chamar: ${error.message}")
                // Aqui você poderia emitir um SideEffect para mostrar Toast de erro
            }
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