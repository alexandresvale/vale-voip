package com.example.valevoip.data.repository

import android.util.Log
import com.example.valevoip.core.lib.linphone.LinphoneManager
import com.example.valevoip.data.SipAccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class SipAccountRepositoryImpl(
    private val linphoneManager: LinphoneManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SipAccountRepository {

    override suspend fun registerAccount(
        userName: String,
        password: String,
        domain: String
    ): Flow<RegistrationState> = callbackFlow {
        Log.d("ALE", "SipAccountRepositoryImpl registerAccount callbackFlow")
        linphoneManager.registerAccount(userName, password, domain) { state ->
            Log.d("ALE", "SipAccountRepositoryImpl registerAccount callbackFlow state = ${state.name}")
            trySend(state).isSuccess
        }
        awaitClose {}
    }.flowOn(dispatcher)

    override suspend fun unregisterAccount() = callbackFlow {
        Log.d("ALE", "SipAccountRepositoryImpl unregisterAccount callbackFlow")
        linphoneManager.unregister { state ->
            Log.d("ALE", "SipAccountRepositoryImpl unregisterAccount callbackFlow state = ${state.name}")
            trySend(state).isSuccess
        }
        awaitClose { }
    }.flowOn(dispatcher)

    override suspend fun accept() {
        linphoneManager.accept()
    }

    override suspend fun makeCall(callee: String, callback: (String) -> Unit) {
        /*linphoneManager.makeCall(callee) { callState ->
            callback.invoke(callState)
        }*/
    }

    override suspend fun getRegistrationState(): StateFlow<RegistrationState?> {
        return linphoneManager.registrationStateFlow
    }

    override suspend fun getCallState(): StateFlow<Call.State?> {
        return linphoneManager.callStateFlow
    }

    override fun removeAccountListener() {
        TODO("Not yet implemented")
    }
}