package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.core.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso responsável por desregistrar a conta do cliente SIP e limpar as credenciais
 * armazenadas localmente no [AccountRepository].
 *
 * Garante que mesmo em caso de falha de rede ao contatar o servidor SIP, a conta seja
 * limpa localmente, permitindo ao usuário efetuar logout e retornar à tela de login.
 */
class UnregisterAccountUseCase @Inject constructor(
    private val sipClient: SipClient,
    private val repository: AccountRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Result<Unit> = withContext(dispatcher) {
        try {
            sipClient.unregister().firstOrNull()
        } catch (_: Exception) {
            // Falhas de rede no desregistro remoto não devem impedir o logout local.
        } finally {
            repository.clearAccount()
        }
        Result.success(Unit)
    }
}
