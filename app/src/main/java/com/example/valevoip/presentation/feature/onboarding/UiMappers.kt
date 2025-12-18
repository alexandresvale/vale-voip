package com.example.valevoip.presentation.feature.onboarding

import com.example.domain.model.RegistrationStatus

fun RegistrationStatus.toUserMessage(): String {
    return when (this) {
        RegistrationStatus.None -> "Aguardando..."
        RegistrationStatus.Progress -> "Conectando ao servidor..."
        RegistrationStatus.Ok -> "Conectado com sucesso!"
        RegistrationStatus.Cleared -> "Registro desconectado."
        RegistrationStatus.Failed -> "Falha na conexão. Verifique os dados."
        RegistrationStatus.Refreshing -> "Atualizando conexão..."
    }
}