package com.valevoip.core.domain.client

import com.valevoip.core.domain.model.CallHistoryItem
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
     * Inicia o motor SIP com as credenciais informadas.
     */
    suspend fun register(account: SipAccount): Flow<SipRegistrationState>

    /**
     * Desconecta do servidor
     */
//    suspend fun unregister()

    //    suspend fun registerUser(username: String, password: String, domain: String): Flow<SipRegistrationState>
    suspend fun unregister(): Flow<SipRegistrationState>
    fun makeCall(number: String): Result<Unit>
    fun hangUp(): Result<Unit>
    fun toggleMute(): Result<Unit>
    fun toggleSpeaker(): Result<Boolean>
    fun muteCall(callId: String): Result<Unit>
    fun acceptCall(): Result<Unit>
    fun getCallStatusFlow(): Flow<CallStatus>
    fun getCurrentCallNumber(): String?
    fun getSynchronousCallStatus(): CallStatus
    fun getCallLogs(): Result<List<CallHistoryItem>>
    fun clearCallLogs(): Result<Unit>
}