package com.example.data.service

import android.util.Log
import com.example.data.mapper.toDomain
import com.example.domain.model.RegistrationStatus
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.linphone.core.AccountListener

class LinphoneService(
    private val linphoneManager: LinphoneManager
) : ValeVoipService {

    override suspend fun registerUser(
        username: String, password: String, domain: String
    ): Flow<RegistrationStatus> = callbackFlow {

        val authInfo = linphoneManager.factory.createAuthInfo(
            username, null, password, null, null, domain, null
        )

        val accountParams = linphoneManager.getAccountParams(username, domain)
        val account = linphoneManager.getCore().createAccount(accountParams)

        with(linphoneManager.getCore()) {
            addAuthInfo(authInfo)
            addAccount(account)
            defaultAccount = account
        }

        val callback = AccountListener { account, state, message ->
            logEvent("[Account] Registration state changed: $state, $message")
            val status = state.toDomain()
            logEvent("[Account] state deferred: $status")
            trySend(status).isSuccess // Envia cada estado para o Flow

            when (status) {
                RegistrationStatus.Ok -> {
                    logEvent("[Account] Registro bem-sucedido! Encerrando fluxo.")
                    close()
                }

                RegistrationStatus.Failed -> {
                    // Unauthorized, io error
                    logEvent("[Account] Falha no registro status = $status")
                    logEvent("[Account] Falha no registro message = $message")
                    close(Exception(message))
                }

                else -> {}
            }
        }

        account.addListener(callback)

        logEvent("Linphone Core ${linphoneManager.getCore().version}")

        awaitClose {
            logEvent("registerUser awaitClose")
            account.removeListener(callback) // Remove o listener quando o Flow for cancelado
        }
    }

    override fun makeCall(number: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun hangUp(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun endCall(callId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun muteCall(callId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "LinphoneManager | $string")
    }
}
