package com.valevoip.app.presentation.feature.call

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.domain.model.CallStatus
import com.valevoip.domain.usecase.AnswerCallUseCase
import com.valevoip.domain.usecase.HangUpUseCase
import com.valevoip.domain.usecase.MakeCallUseCase
import com.valevoip.domain.usecase.ObserveCallStateUseCase
import com.valevoip.domain.usecase.ToggleMuteUseCase
import com.valevoip.domain.usecase.ToggleSpeakerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val makeCallUseCase: MakeCallUseCase,
    private val hangUpUseCase: HangUpUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val toggleMuteUseCase: ToggleMuteUseCase,
    private val toggleSpeakerUseCase: ToggleSpeakerUseCase,
    private val observeCallStateUseCase: ObserveCallStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val number = savedStateHandle.get<String>("number") ?: ""
        val isIncoming = savedStateHandle.get<Boolean>("isIncoming") ?: false

        val initialStatus = if (isIncoming) CallStatus.INCOMING else CallStatus.DIALING
        _uiState.update {
            it.copy(
                contactNumber = number,
                callStatus = initialStatus
            )
        }
        observeCallStatus()
        if (!isIncoming) {
            Log.d("CallViewModel", "Modo Discagem: Iniciando chamada para $number")
            startCall(number)
        } else {
            Log.d("CallViewModel", "Modo Recebimento: Apenas observando chamada de $number")
        }
    }

    fun onEvent(event: CallUiEvent) {
        when (event) {
            CallUiEvent.OnHangup -> performHangup()
            CallUiEvent.OnToggleMute -> performToggleMute()
            CallUiEvent.OnToggleSpeaker -> performToggleSpeaker()
            CallUiEvent.OnShowKeypad -> {}
            CallUiEvent.OnAnswer -> performAnswer()
        }
    }

    private fun startCall(number: String) {
        val number = when (number) {
            "2525" -> "valevoipios"
            "5555" -> "alexandreskt16"
            else -> number
        }
        makeCallUseCase(number)
            .onSuccess {
                Log.d("CallViewModel", "Chamada enviada com sucesso para o core.")
            }
            .onFailure { error ->
                Log.e("CallViewModel", "Erro ao chamar: ${error.message}")
                _uiState.update {
                    it.copy(callStatus = CallStatus.ENDED)
                }
            }
    }

    private fun performHangup() {
        _uiState.update { it.copy(callStatus = CallStatus.ENDED) }
        stopTimer()
        hangUpUseCase()
            .onSuccess {
                Log.d("CallViewModel", "Comando de desligar enviado com sucesso.")
                _uiState.update { it.copy(callStatus = CallStatus.ENDED) }
            }
            .onFailure { e ->
                Log.e("CallViewModel", "Falha ao enviar comando de desligar: ${e.message}")
                _uiState.update { it.copy(callStatus = CallStatus.ENDED) }
            }
    }

    private fun performAnswer() {
        answerCallUseCase()
            .onSuccess {
                Log.d("CallViewModel", "Comando de atender enviado.")
            }
            .onFailure { e ->
                Log.e("CallViewModel", "Falha ao atender: ${e.message}")
            }
    }

    private fun performToggleMute() {
        val newMuteState = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newMuteState) }
        toggleMuteUseCase()
            .onSuccess {
                Log.d("CallViewModel", "Mute alterado com sucesso para: $newMuteState")
            }
            .onFailure {
                Log.e("CallViewModel", "Falha ao alterar mute.")
            }
    }

    private fun performToggleSpeaker() {
        val newSpeakerState = !_uiState.value.isSpeakerOn
        _uiState.update { it.copy(isSpeakerOn = newSpeakerState) }
        toggleSpeakerUseCase()
            .onSuccess {
                Log.d("CallViewModel", "Viva-voz alterado para: $newSpeakerState")
            }
            .onFailure {
                Log.e("CallViewModel", "Falha ao mudar viva-voz")
                // 3. Rollback: Se falhou, desfaz a mudança visual
                _uiState.update { it.copy(isSpeakerOn = !newSpeakerState) }
            }
    }

    private fun startTimer() {
        // Se o timer já estiver rodando, não reinicia (Evita resetar para 00:00 se o status piscar)
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            Log.d("CallViewModel", "Timer iniciado")
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
                    Log.d("CallViewModel", "Abrindo tela para chamada existente. Status: $newStatus")
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
}