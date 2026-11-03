package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.DeviceInfoProvider
import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.NetworkConnectivityState
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.SipTransport
import com.valevoip.core.domain.model.VoipSettings
import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateDiagnosticReportUseCaseTest {

    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeSipClient: FakeSipClient
    private lateinit var fakeDeviceInfoProvider: FakeDeviceInfoProvider
    private lateinit var fakeNetworkConnectivityProvider: FakeNetworkConnectivityProvider
    private lateinit var useCase: GenerateDiagnosticReportUseCase

    @Before
    fun setUp() {
        fakeAccountRepository = FakeAccountRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        fakeSipClient = FakeSipClient()
        fakeDeviceInfoProvider = FakeDeviceInfoProvider()
        fakeNetworkConnectivityProvider = FakeNetworkConnectivityProvider()

        useCase = GenerateDiagnosticReportUseCase(
            accountRepository = fakeAccountRepository,
            settingsRepository = fakeSettingsRepository,
            sipClient = fakeSipClient,
            deviceInfoProvider = fakeDeviceInfoProvider,
            networkConnectivityProvider = fakeNetworkConnectivityProvider
        )
    }

    @Test
    fun `invoke generates complete report with account, device and network info`() = runTest {
        fakeAccountRepository.account = SipAccount(
            username = "2001",
            password = "secret_password",
            domain = "pbx.empresa.com",
            transport = SipTransport.TLS
        )
        fakeDeviceInfoProvider.appVersion = "1.2.0"
        fakeDeviceInfoProvider.manufacturer = "Google"
        fakeDeviceInfoProvider.model = "Pixel 8"
        fakeDeviceInfoProvider.osVersion = "14"
        fakeDeviceInfoProvider.apiLevel = 34
        fakeNetworkConnectivityProvider.isConnected = true
        fakeNetworkConnectivityProvider.isWifiConnected = true
        fakeSipClient.registrationStateFlow.value = SipRegistrationState.Ok
        fakeSipClient.version = "5.3.10"

        val report = useCase()

        // Verifica cabeçalho e dispositivo
        assertTrue(report.contains("=== Vale VoIP - Relatório de Diagnóstico ==="))
        assertTrue(report.contains("Versão do App: 1.2.0"))
        assertTrue(report.contains("Fabricante: Google"))
        assertTrue(report.contains("Modelo: Pixel 8"))
        assertTrue(report.contains("Android: 14 (API 34)"))

        // Verifica rede
        assertTrue(report.contains("Conexão Ativa: Sim"))
        assertTrue(report.contains("Tipo de Rede: Wi-Fi"))
        assertTrue(report.contains("Modo 'Apenas Wi-Fi': Desativado"))

        // Verifica conta (garantindo que a senha NÃO vaze)
        assertTrue(report.contains("Ramal: 2001"))
        assertTrue(report.contains("Domínio: pbx.empresa.com"))
        assertTrue(report.contains("Transporte: TLS"))
        assertFalse(report.contains("secret_password"))

        // Verifica motor VoIP
        assertTrue(report.contains("Versão Liblinphone: 5.3.10"))
        assertTrue(report.contains("Cancelamento de Eco (AEC): Ativado"))
        assertTrue(report.contains("Vibrar ao Chamar: Ativado"))
    }

    @Test
    fun `invoke indicates cellular data when wifi is disconnected`() = runTest {
        fakeNetworkConnectivityProvider.isConnected = true
        fakeNetworkConnectivityProvider.isWifiConnected = false

        val report = useCase()

        assertTrue(report.contains("Tipo de Rede: Dados Móveis (Celular)"))
    }

    @Test
    fun `invoke indicates offline when there is no internet connection`() = runTest {
        fakeNetworkConnectivityProvider.isConnected = false
        fakeNetworkConnectivityProvider.isWifiConnected = false

        val report = useCase()

        assertTrue(report.contains("Conexão Ativa: Não"))
        assertTrue(report.contains("Tipo de Rede: Sem conexão com a Internet"))
    }

    // --- Fakes ---
    private class FakeAccountRepository : AccountRepository {
        var account: SipAccount? = null
        override suspend fun getAccount(): SipAccount? = account
        override suspend fun insertAccount(sipAccount: SipAccount) { this.account = sipAccount }
        override suspend fun clearAccount() { account = null }
    }

    private class FakeSettingsRepository : SettingsRepository {
        override var currentSettings: VoipSettings = VoipSettings()
        override val settings: Flow<VoipSettings> = emptyFlow()
        override suspend fun setEchoCancellation(enabled: Boolean) { currentSettings = currentSettings.copy(echoCancellationEnabled = enabled) }
        override suspend fun setVibrate(enabled: Boolean) { currentSettings = currentSettings.copy(vibrateOnIncomingCall = enabled) }
        override suspend fun setWifiOnly(enabled: Boolean) { currentSettings = currentSettings.copy(useWifiOnly = enabled) }
    }

    private class FakeDeviceInfoProvider : DeviceInfoProvider {
        override var appVersion: String = "1.0.0"
        override var manufacturer: String = "Generic"
        override var model: String = "TestDevice"
        override var osVersion: String = "13"
        override var apiLevel: Int = 33
    }

    private class FakeNetworkConnectivityProvider : NetworkConnectivityProvider {
        override var isWifiConnected: Boolean = true
        override var isConnected: Boolean = true
        override val connectivityState: Flow<NetworkConnectivityState> = emptyFlow()
    }

    private class FakeSipClient : SipClient {
        val registrationStateFlow = MutableStateFlow<SipRegistrationState>(SipRegistrationState.None)
        var version: String = "5.0.0"
        override val registrationState: StateFlow<SipRegistrationState> = registrationStateFlow
        override suspend fun register(account: SipAccount): Flow<SipRegistrationState> = emptyFlow()
        override suspend fun unregister(): Flow<SipRegistrationState> = emptyFlow()
        override fun makeCall(number: String): Result<Unit> = Result.success(Unit)
        override fun hangUp(): Result<Unit> = Result.success(Unit)
        override fun toggleMute(): Result<Unit> = Result.success(Unit)
        override fun toggleSpeaker(): Result<Boolean> = Result.success(true)
        override fun muteCall(callId: String): Result<Unit> = Result.success(Unit)
        override fun acceptCall(): Result<Unit> = Result.success(Unit)
        override fun getCallStatusFlow(): Flow<CallStatus> = emptyFlow()
        override fun getCurrentCallNumber(): String? = null
        override fun getSynchronousCallStatus(): CallStatus = CallStatus.IDLE
        override fun getLinphoneVersion(): String = version
    }
}
