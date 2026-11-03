package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.repository.SettingsRepository
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val sipClient: SipClient,
    private val settingsRepository: SettingsRepository,
    private val networkConnectivityProvider: NetworkConnectivityProvider
) {
    operator fun invoke(number: String): Result<Unit> {
        val settings = settingsRepository.currentSettings
        if (settings.useWifiOnly && !networkConnectivityProvider.isWifiConnected) {
            return Result.failure(
                IllegalStateException("Chamada bloqueada: a opção 'Usar Somente Wi-Fi' está ativada e o dispositivo não está conectado a uma rede Wi-Fi.")
            )
        }
        return sipClient.makeCall(number)
    }
}
