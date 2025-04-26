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
        logEvent("Thread atual: ${Thread.currentThread().name}")

        awaitClose {
            logEvent("registerUser awaitClose")
            account.removeListener(callback) // Remove o listener quando o Flow for cancelado
        }
    }

    override suspend fun unregister(): Flow<RegistrationStatus> = callbackFlow {
        logEvent("[Account] Unregister")
        val account = linphoneManager.getCore().defaultAccount
        logEvent("[Account] Unregister account = $account")

        if (account == null) {
            logEvent("[Account] Não há conta")
            close(Exception("Não há conta")) // Fecha o Flow e propaga a exceção
            return@callbackFlow
        }

        logEvent("[Account] Unregister account isRegisterEnabled = ${account.params.isRegisterEnabled}")

        val accountParams = account.params.clone()
        accountParams.isRegisterEnabled = false
        account.params = accountParams
        val callback = AccountListener { _, state, message ->
            logEvent("[Account] Unregister state changed: $state, $message")
            val status = state.toDomain()
            trySend(status).isSuccess

            when (status) {
                RegistrationStatus.Cleared -> {
                    logEvent("[Account] Unregister bem-sucedido! Encerrando fluxo.")
                    close()
                }

                RegistrationStatus.Failed -> {
                    logEvent("[Account] Unregister falha status = $status")
                    logEvent("[Account] Unregister falha message = $message")
                    close(Exception(message))
                }

                else -> {}
            }
        }
        account.addListener(callback)
        awaitClose {
            logEvent("Unregister awaitClose")
            account.removeListener(callback)
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
