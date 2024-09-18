package com.example.valevoip.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.Call
import org.linphone.core.RegistrationState

interface SipAccountRepository {
    suspend fun registerAccount(userName: String, password: String, domain: String): Flow<RegistrationState>
    suspend fun unregisterAccount(): Flow<RegistrationState>
    fun removeAccountListener()
    suspend fun getRegistrationState(): StateFlow<RegistrationState?>
    suspend fun getCallState(): StateFlow<Call.State?>
    suspend fun accept()
    suspend fun makeCall(callee: String, callback: (String) -> Unit)
}