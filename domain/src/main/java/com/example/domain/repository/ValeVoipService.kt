package com.example.domain.repository

import com.example.domain.model.RegistrationStatus
import kotlinx.coroutines.flow.Flow

interface ValeVoipService {
    suspend fun registerUser(username: String, password: String, domain: String): Flow<RegistrationStatus>
    fun makeCall(number: String): Result<Unit>
    fun hangUp(): Result<Unit>
//    fun onIncomingCall(listener: (Call) -> Unit)
//fun startCall(destination: String): Result<Call>
    fun endCall(callId: String): Result<Unit>
    fun muteCall(callId: String): Result<Unit>
}