package com.example.data.service

import android.util.Log
import com.example.data.mapper.toDomain
import com.example.domain.model.CallStatus
import com.example.domain.model.RegistrationStatus
import com.example.domain.repository.ValeVoipService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform
import org.linphone.core.Call

internal class LinphoneService(
    private val linphoneManager: LinphoneManager
) : ValeVoipService {

    override suspend fun registerUser(
        username: String, password: String, domain: String
    ): Flow<RegistrationStatus> {
        return linphoneManager.registerAccount(username, domain, password)
            .map { sdkState ->
                sdkState.toDomain()
            }
            .transform { status ->
                emit(status)
                when (status) {
                    RegistrationStatus.Ok -> {
                        logEvent("Registro concluído com sucesso.")
                    }

                    RegistrationStatus.Failed -> {
                        logEvent("Falha no registro.")
                        throw Exception("Falha ao registrar na SDK") // Ou emitir erro customizado
                    }

                    else -> {}
                }
            }
    }

    override suspend fun unregister(): Flow<RegistrationStatus> {
        logEvent("[Account] Unregister")
        return linphoneManager.unregisterAccount()
            .map { it.toDomain() }
            .transform { status ->
                emit(status)
                if (status == RegistrationStatus.Cleared) {
                    logEvent("Unregister concluído.")
                }
                if (status == RegistrationStatus.Failed) {
                    logEvent("Falha no unregister.")
                    throw Exception("Falha ao unregister na SDK")
                }
            }
    }


    override fun makeCall(number: String): Result<Unit> {
        logEvent("[Call] Tentando iniciar chamada para: $number")
        return try {
            if (!linphoneManager.isNetworkAvailable()) {
                return Result.failure(Exception("Sem conexão com a internet."))
            }

            if (number.isBlank()) {
                return Result.failure(IllegalArgumentException("Número inválido."))
            }

            val success = linphoneManager.invite(number)

            if (success) {
                logEvent("Chamada iniciada para $number")
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Erro na SDK ao tentar discar (URI inválida?)"))
            }
        } catch (e: Exception) {
            logEvent("[Call] Exceção crítica ao tentar chamar: ${e.message}")
            Result.failure(e)
        }
    }

    override fun hangUp(): Result<Unit> {
        return try {
            linphoneManager.terminateCurrentCall()
            Result.success(Unit)
        } catch (e: Exception) {
            logEvent("[Call] Erro ao tentar desligar: ${e.message}")
            Result.failure(e)
        }
    }

    override fun acceptCall(): Result<Unit> {
        linphoneManager.acceptCall()
        return Result.success(Unit)
    }

    override fun toggleMute(): Result<Unit> {
        return try {
            linphoneManager.toggleMicrophone()
            logEvent("[Audio] Microfone alterado. Mutado:")
            Result.success(Unit)
        } catch (e: Exception) {
            logEvent("[Audio] Erro ao mutar: ${e.message}")
            Result.failure(e)
        }
    }

    override fun toggleSpeaker(): Result<Boolean> {
        return try {
            val isSpeakerOn = linphoneManager.toggleSpeaker()
            logEvent("[Audio] Saída de áudio alterada. Speaker Ativo: $isSpeakerOn")
            Result.success(isSpeakerOn)
        } catch (e: Exception) {
            logEvent("[Audio] Erro ao alternar speaker: ${e.message}")
            Result.failure(e)
        }
    }

    override fun muteCall(callId: String): Result<Unit> {
        return toggleMute().map { }
    }

    override fun getCallStatusFlow(): Flow<CallStatus> {
        return linphoneManager.observeCoreCallState().mapNotNull { sdkState ->
            when (sdkState) {
                Call.State.OutgoingInit,
                Call.State.OutgoingProgress -> CallStatus.DIALING

                Call.State.OutgoingRinging -> CallStatus.RINGING
                Call.State.Connected,
                Call.State.StreamsRunning -> CallStatus.ACTIVE

                Call.State.IncomingReceived -> CallStatus.INCOMING

                Call.State.End,
                Call.State.Released,
                Call.State.Error -> CallStatus.ENDED
                // Filtra estados que o domínio não liga (Pause, Resuming, etc)
                else -> null
            }
        }
    }

    override fun getCurrentCallNumber(): String? {
        return linphoneManager.getCurrentCallNumber()
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "LinphoneManager | $string")
    }
}
