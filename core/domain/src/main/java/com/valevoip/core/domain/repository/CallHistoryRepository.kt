package com.valevoip.core.domain.repository

import com.valevoip.core.domain.model.CallHistoryItem

/**
 * Contrato de repositório para persistência e consulta do histórico de chamadas.
 *
 * Seguindo a Clean Architecture, abstrai a fonte de dados (banco SQLite interno do Linphone,
 * Room ou backend) e garante contratos assíncronos e seguros para a camada de domínio.
 */
interface CallHistoryRepository {
    suspend fun getCallHistory(): Result<List<CallHistoryItem>>
    suspend fun deleteCallHistoryItem(id: String): Result<Unit>
    suspend fun clearCallHistory(): Result<Unit>
}
