package com.example.domain.repository

import com.example.domain.model.CallStatus
import com.example.domain.model.RegistrationStatus
import kotlinx.coroutines.flow.Flow

interface ValeVoipService {
    suspend fun registerUser(username: String, password: String, domain: String): Flow<RegistrationStatus>
    suspend fun unregister(): Flow<RegistrationStatus>
    fun makeCall(number: String): Result<Unit>
    fun hangUp(): Result<Unit>
    fun toggleMute(): Result<Unit>
    fun toggleSpeaker(): Result<Boolean>
    fun muteCall(callId: String): Result<Unit>
    fun acceptCall(): Result<Unit>
    fun getCallStatusFlow(): Flow<CallStatus>
    fun getCurrentCallNumber(): String?
}