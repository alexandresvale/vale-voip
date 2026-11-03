package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.DeviceInfoProvider
import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.core.domain.repository.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Caso de uso responsável por consolidar e estruturar as informações técnicas de diagnóstico
 * para compartilhamento com a equipe de suporte do Vale VoIP.
 *
 * Consolida dados do dispositivo, estado de conectividade de rede, parâmetros SIP e
 * configurações de áudio ativas, sem vazar credenciais ou dados sensíveis (LGPD).
 */
class GenerateDiagnosticReportUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val sipClient: SipClient,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val networkConnectivityProvider: NetworkConnectivityProvider
) {
    suspend operator fun invoke(): String {
        val account = accountRepository.getAccount()
        val settings = settingsRepository.currentSettings
        val regState = sipClient.registrationState.value
        val isConnected = networkConnectivityProvider.isConnected
        val isWifi = networkConnectivityProvider.isWifiConnected
        val networkType = when {
            !isConnected -> "Sem conexão com a Internet"
            isWifi -> "Wi-Fi"
            else -> "Dados Móveis (Celular)"
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        return buildString {
            appendLine("=== Vale VoIP - Relatório de Diagnóstico ===")
            appendLine("Data e Hora: ${dateFormat.format(Date())}")
            appendLine()
            appendLine("--- APLICATIVO E DISPOSITIVO ---")
            appendLine("Versão do App: ${deviceInfoProvider.appVersion}")
            appendLine("Fabricante: ${deviceInfoProvider.manufacturer}")
            appendLine("Modelo: ${deviceInfoProvider.model}")
            appendLine("Android: ${deviceInfoProvider.osVersion} (API ${deviceInfoProvider.apiLevel})")
            appendLine()
            appendLine("--- REDE E CONECTIVIDADE ---")
            appendLine("Conexão Ativa: ${if (isConnected) "Sim" else "Não"}")
            appendLine("Tipo de Rede: $networkType")
            appendLine("Modo 'Apenas Wi-Fi': ${if (settings.useWifiOnly) "Ativado" else "Desativado"}")
            appendLine()
            appendLine("--- CONTA SIP ---")
            appendLine("Ramal: ${account?.username ?: "Não configurado"}")
            appendLine("Domínio: ${account?.domain ?: "Não configurado"}")
            appendLine("Transporte: ${account?.transport?.name ?: "N/A"}")
            appendLine("Estado de Registro: $regState")
            appendLine()
            appendLine("--- MOTOR VOIP (LINPHONE) ---")
            appendLine("Versão Liblinphone: ${sipClient.getLinphoneVersion()}")
            appendLine("Estado da Chamada: ${sipClient.getSynchronousCallStatus()}")
            appendLine("Cancelamento de Eco (AEC): ${if (settings.echoCancellationEnabled) "Ativado" else "Desativado"}")
            appendLine("Vibrar ao Chamar: ${if (settings.vibrateOnIncomingCall) "Ativado" else "Desativado"}")
        }
    }
}
