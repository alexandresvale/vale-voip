package com.valevoip.feature.call

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCallStatusSyncUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.MakeCallUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ToggleMuteUseCase
import com.valevoip.core.domain.usecase.ToggleSpeakerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CallViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val makeCallUseCase: MakeCallUseCase,
    private val hangUpUseCase: HangUpUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val toggleMuteUseCase: ToggleMuteUseCase,
    private val toggleSpeakerUseCase: ToggleSpeakerUseCase,
    private val observeCallStateUseCase: ObserveCallStateUseCase,
    private val getCallStatusSyncUseCase: GetCallStatusSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hasInitiatedCall = false

    init {
        observeCallStatus()
    }

    fun onEvent(event: CallUiEvent) {
        when (event) {
            CallUiEvent.OnPermissionGranted -> handleCallInitialization()
            CallUiEvent.OnHangup -> performHangup()
            CallUiEvent.OnToggleMute -> performToggleMute()
            CallUiEvent.OnToggleSpeaker -> performToggleSpeaker()
            CallUiEvent.OnShowKeypad -> {}
            CallUiEvent.OnAnswer -> performAnswer()
        }
    }

    private fun handleCallInitialization() {
        logEvent("Iniciando avaliação de chamada (handleCallInitialization)")
        val number = savedStateHandle.get<String>("number")
        if (!number.isNullOrBlank() && !hasInitiatedCall) {
            hasInitiatedCall = true
            _uiState.update { it.copy(contactNumber = number) }
            startCall(number)
        } else if (number.isNullOrBlank()) {
            logEvent("Modo Recebimento detectado (Número vazio na rota). Aguardando Linphone enviar os dados.")
        }
    }

    private fun startCall(number: String) {
        logEvent("startCall - $number")
        val number = when (number) {
            "2525" -> "valevoipios"
            "5555" -> "alexandreskt16"
            else -> number
        }
        makeCallUseCase(number)
            .onSuccess {
                logEvent("Chamada enviada com sucesso para o core.")
            }
            .onFailure { error ->
                logEvent("Erro ao chamar: ${error.message}")
                _uiState.update {
                    it.copy(callStatus = CallStatus.ENDED)
                }
            }
    }

    private fun performHangup() {
        logEvent("Botão de desligar pressionado. Solicitando ao core...")
        hangUpUseCase()
            .onSuccess {
                logEvent("Comando de desligar aceito pelo SIP. Aguardando evento de término...")
            }
            .onFailure { e ->
                logEvent("Falha crítica ao desligar: ${e.message}. Forçando encerramento da tela.")
                // Se falhar em se comunicar com o C++, forçamos o fim para não travar o app
                _uiState.update { it.copy(callStatus = CallStatus.ENDED) }
            }
    }

    private fun performAnswer() {
        answerCallUseCase()
            .onSuccess {
                logEvent("Comando de atender enviado.")
            }
            .onFailure { e ->
                logEvent("Falha ao atender: ${e.message}")
            }
    }

    private fun performToggleMute() {
        val newMuteState = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newMuteState) }
        toggleMuteUseCase()
            .onSuccess {
                logEvent("Mute alterado com sucesso para: $newMuteState")
            }
            .onFailure {
                logEvent("Falha ao alterar mute.")
                _uiState.update { it.copy(isMuted = !newMuteState) }
            }
    }

    private fun performToggleSpeaker() {
        val newSpeakerState = !_uiState.value.isSpeakerOn
        _uiState.update { it.copy(isSpeakerOn = newSpeakerState) }
        toggleSpeakerUseCase()
            .onSuccess {
                logEvent("Viva-voz alterado para: $newSpeakerState")
            }
            .onFailure {
                logEvent("Falha ao mudar viva-voz")
                // 3. Rollback: Se falhou, desfaz a mudança visual
                _uiState.update { it.copy(isSpeakerOn = !newSpeakerState) }
            }
    }

    private fun startTimer() {
        // Se o timer já estiver rodando, não reinicia (Evita resetar para 00:00 se o status piscar)
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            logEvent("Timer iniciado")
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val newDuration = state.duration + 1
                    state.copy(
                        duration = newDuration,
                        formattedDuration = formatDuration(newDuration)
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun formatDuration(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    private fun observeCallStatus() {
        viewModelScope.launch {
            observeCallStateUseCase()
                .collect { newStatus ->
                    logEvent("Abrindo tela para chamada existente. Status: $newStatus")
                    _uiState.update { it.copy(callStatus = newStatus) }
                    when (newStatus) {
                        CallStatus.INCOMING -> {}
                        CallStatus.ACTIVE -> startTimer()
                        CallStatus.ENDED -> stopTimer()
                        else -> Unit
                    }
                }
        }
    }

    private fun logEvent(string: String) {
        Log.d("VALEVOIP_TAG", "CallViewModel | $string")
    }
}