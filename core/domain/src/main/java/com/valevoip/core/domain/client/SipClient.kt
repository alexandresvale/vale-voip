package com.valevoip.core.domain.client

import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SipClient {
    /**
     * Fluxo contínuo que emite o estado de registro atual
     */
    val registrationState: StateFlow<SipRegistrationState>

    /**
     * Fluxo contínuo que emite se o microfone da chamada atual está silenciado
     */
    val isMuted: StateFlow<Boolean>

    /**
     * Fluxo contínuo que emite se a saída de áudio atual é o viva-voz (alto-falante)
     */
    val isSpeakerEnabled: StateFlow<Boolean>

    /**
     * Lista reativa das rotas de saída de áudio disponíveis para chamada (Telefone, Viva-voz, Bluetooth, Fone)
     */
    val availableAudioRoutes: StateFlow<List<AudioRoute>>

    /**
     * Rota de saída de áudio ativa no momento
     */
    val activeAudioRoute: StateFlow<AudioRoute?>

    /**
     * Inicia o motor SIP com as credenciais informadas.
     */
    suspend fun register(account: SipAccount): Flow<SipRegistrationState>

    /**
     * Desconecta do servidor
     */
    suspend fun unregister(): Flow<SipRegistrationState>
    fun makeCall(number: String): Result<Unit>
    fun hangUp(): Result<Unit>
    fun toggleMute(): Result<Unit>
    fun toggleSpeaker(): Result<Boolean>
    fun selectAudioRoute(routeId: String): Result<Unit>
    fun sendDtmf(digit: Char): Result<Unit>
    fun toggleHold(): Result<Boolean>
    fun muteCall(callId: String): Result<Unit>
    fun acceptCall(): Result<Unit>
    fun getCallStatusFlow(): Flow<CallStatus>
    fun getCurrentCallNumber(): String?
    fun getSynchronousCallStatus(): CallStatus
    fun getLinphoneVersion(): String
}
