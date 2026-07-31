package com.valevoip.feature.login

import com.valevoip.core.domain.model.SipRegistrationState

fun SipRegistrationState.toUserMessage(): String {
    return when (this) {
        is SipRegistrationState.None -> ""
        is SipRegistrationState.Progress -> "Aguardando..."
        is SipRegistrationState.Refreshing -> "Conectando ao servidor..."
        is SipRegistrationState.Ok -> "Conectado com sucesso!"
        is SipRegistrationState.Failed -> "Falha na conexão: ${this.reason}"
        is SipRegistrationState.Cleared -> "Desconectado"
    }
}