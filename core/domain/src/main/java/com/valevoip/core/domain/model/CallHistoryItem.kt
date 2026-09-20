package com.valevoip.core.domain.model

data class CallHistoryItem(
    val id: String, // Identificador único (pode ser o callId)
    val remoteAddress: String, // O número ou SIP URI
    val displayName: String?, // Nome do contato (se tiver)
    val direction: CallDirection,
    val status: CallHistoryStatus,
    val timestamp: Long, // Data em milissegundos
    val durationSeconds: Int
)

enum class CallDirection {
    INCOMING, OUTGOING
}

enum class CallHistoryStatus {
    SUCCESS, // Conversou
    MISSED,  // Perdida
    DECLINED // Rejeitada/Cancelada
}