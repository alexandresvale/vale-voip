package com.valevoip.feature.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCallStatusSyncUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.MakeCallUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ObserveMuteStateUseCase
import com.valevoip.core.domain.usecase.ObserveSpeakerStateUseCase
import com.valevoip.core.domain.usecase.ToggleMuteUseCase
import com.valevoip.core.domain.usecase.ToggleSpeakerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class CallViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val makeCallUseCase: MakeCallUseCase,
    private val hangUpUseCase: HangUpUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val toggleMuteUseCase: ToggleMuteUseCase,
    private val toggleSpeakerUseCase: ToggleSpeakerUseCase,
    private val observeCallStateUseCase: ObserveCallStateUseCase,
    private val getCallStatusSyncUseCase: GetCallStatusSyncUseCase,
    private val getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase,
    private val observeMuteStateUseCase: ObserveMuteStateUseCase,
    private val observeSpeakerStateUseCase: ObserveSpeakerStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CallUiState(
            contactNumber = savedStateHandle.get<String>("number").orEmpty()
        )
    )
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hasInitiatedCall = false
    private var hasObservedLiveCall = false

    init {
        hydrateRemoteNumberIfMissing()
        observeAudioStates()
        observeCallStatus()
    }

    fun onEvent(event: CallUiEvent) {
        when (event) {
            CallUiEvent.OnPermissionGranted -> handleCallInitialization()
            CallUiEvent.OnHangup -> performHangup()
            CallUiEvent.OnToggleMute -> performToggleMute()
            CallUiEvent.OnToggleSpeaker -> performToggleSpeaker()
            CallUiEvent.OnShowKeypad -> Unit
            CallUiEvent.OnAnswer -> performAnswer()
        }
    }

    private fun handleCallInitialization() {
        logEvent("Iniciando avaliação de chamada (handleCallInitialization)")
        val number = savedStateHandle.get<String>("number")

        // Proteção: Se já temos uma chamada ativa ou tocando (via SIP),
        // não devemos iniciar uma nova chamada de saída!
        val currentStatus = getCallStatusSyncUseCase()
        if (currentStatus != CallStatus.IDLE && currentStatus != CallStatus.ENDED) {
            hasObservedLiveCall = true
            logEvent("Já existe uma chamada no estado: $currentStatus. Apenas exibindo a tela.")
            if (!number.isNullOrBlank()) {
                _uiState.update { it.copy(contactNumber = number) }
            } else {
                hydrateRemoteNumberIfMissing()
            }
            return
        }

        if (!number.isNullOrBlank() && !hasInitiatedCall) {
            hasInitiatedCall = true
            _uiState.update { it.copy(contactNumber = number) }
            startCall(number)
        } else if (number.isNullOrBlank()) {
            hydrateRemoteNumberIfMissing()
            logEvent("Modo Recebimento detectado (Número vazio na rota). Aguardando Linphone enviar os dados.")
        }
    }

    private fun startCall(number: String) {
        logEvent("startCall - $number")
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
        val previousMuteState = _uiState.value.isMuted
        _uiState.update { it.copy(isMuted = !previousMuteState) }
        toggleMuteUseCase()
            .onSuccess {
                logEvent("Mute alterado com sucesso para: ${!previousMuteState}")
            }
            .onFailure {
                logEvent("Falha ao alterar mute.")
                _uiState.update { it.copy(isMuted = previousMuteState) }
            }
    }

    private fun performToggleSpeaker() {
        val previousSpeakerState = _uiState.value.isSpeakerOn
        _uiState.update { it.copy(isSpeakerOn = !previousSpeakerState) }
        toggleSpeakerUseCase()
            .onSuccess { confirmedState ->
                logEvent("Viva-voz alterado para: $confirmedState")
                _uiState.update { it.copy(isSpeakerOn = confirmedState) }
            }
            .onFailure {
                logEvent("Falha ao mudar viva-voz")
                _uiState.update { it.copy(isSpeakerOn = previousSpeakerState) }
            }
    }

    private fun observeAudioStates() {
        viewModelScope.launch {
            observeMuteStateUseCase().collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }
        viewModelScope.launch {
            observeSpeakerStateUseCase().collect { speakerEnabled ->
                _uiState.update { it.copy(isSpeakerOn = speakerEnabled) }
            }
        }
    }

    private fun hydrateRemoteNumberIfMissing() {
        val currentContact = _uiState.value.contactNumber
        if (currentContact.isBlank() || currentContact == UNKNOWN_CALLER_FALLBACK) {
            val remoteNumber = getCurrentCallNumberUseCase()
            if (remoteNumber.isNotBlank()) {
                _uiState.update { it.copy(contactNumber = remoteNumber) }
            }
        }
    }

    private fun startTimer() {
        // Se o timer já estiver rodando, não reinicia (Evita resetar para 00:00 se o status piscar)
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            logEvent("Timer iniciado")
            while (true) {
                delay(1000.milliseconds)
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
                    val routeNumber = savedStateHandle.get<String>("number")
                    val isStaleEndedBeforeOutgoingInit = newStatus == CallStatus.ENDED &&
                        !hasObservedLiveCall &&
                        !hasInitiatedCall &&
                        !routeNumber.isNullOrBlank()

                    if (isStaleEndedBeforeOutgoingInit) {
                        logEvent("Ignorando CallStatus.ENDED residual da chamada anterior antes da discagem.")
                        return@collect
                    }

                    logEvent("Status de chamada atualizado: $newStatus")
                    when (newStatus) {
                        CallStatus.INCOMING,
                        CallStatus.DIALING,
                        CallStatus.RINGING -> {
                            hasObservedLiveCall = true
                            hydrateRemoteNumberIfMissing()
                        }

                        CallStatus.ACTIVE -> {
                            hasObservedLiveCall = true
                            hydrateRemoteNumberIfMissing()
                            startTimer()
                        }

                        CallStatus.ENDED -> stopTimer()
                        CallStatus.IDLE -> Unit
                    }
                    _uiState.update { it.copy(callStatus = newStatus) }
                }
        }
    }

    private fun logEvent(message: String) {
        Timber.d("CallViewModel | %s", message)
    }

    private companion object {
        const val UNKNOWN_CALLER_FALLBACK = "Desconhecido"
    }
}
