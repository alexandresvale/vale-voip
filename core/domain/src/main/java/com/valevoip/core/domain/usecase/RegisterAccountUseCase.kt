package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

sealed interface RegisterError {
    data object EmptyUsername : RegisterError
    data object EmptyPassword : RegisterError
    data object EmptyDomain : RegisterError
    data object InvalidDomain : RegisterError
    data class SipClientError(val cause: Throwable) : RegisterError
}

sealed interface RegisterResult {
    data object Success : RegisterResult
    data class Error(val error: RegisterError) : RegisterResult
}

/**
 * Caso de uso - Registrar Usuário
 */
class RegisterAccountUseCase @Inject constructor(
    private val sipClient: SipClient,
    private val accountRepository: AccountRepository
) {
    // Regex para validar domínio
    private val domainRegex = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$".toRegex()

    // Regex para validar IPv4 (ex: 192.168.1.100)
    private val ipRegex =
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()

    /**
     * Recebe os dados crus da interface (ViewModel).
     * Retorna um booleano (ou um Result) dizendo se o pedido foi aceito.
     */
    suspend operator fun invoke(
        username: String,
        password: String,
        domain: String
    ): RegisterResult {
        // 1. Regra de Negócio: Validação Rígida
        if (username.isBlank()) return RegisterResult.Error(RegisterError.EmptyUsername)
        if (password.isBlank()) return RegisterResult.Error(RegisterError.EmptyPassword)
        if (domain.isBlank()) return RegisterResult.Error(RegisterError.EmptyDomain)
        if (!isValidHost(domain)) return RegisterResult.Error(RegisterError.InvalidDomain)

        // 2. Criação da Entidade no lugar certo
        val account = SipAccount(
            username = username.trim(),
            password = password.trim(),
            domain = domain.trim()
        )

        // Se a regra passar, mandamos para o cliente SIP:
        return try {
            val result = withTimeout(60.seconds) {
                sipClient.register(account).first { it is SipRegistrationState.Ok || it is SipRegistrationState.Failed }
            }

            if (result is SipRegistrationState.Ok) {
                accountRepository.insertAccount(account)
                RegisterResult.Success
            } else {
                val errorMsg = (result as? SipRegistrationState.Failed)?.reason ?: "Falha ao registar"
                RegisterResult.Error(RegisterError.SipClientError(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            RegisterResult.Error(RegisterError.SipClientError(e))
        }
    }

    private fun isValidHost(host: String): Boolean {
        return domainRegex.matches(host) || ipRegex.matches(host)
    }
}