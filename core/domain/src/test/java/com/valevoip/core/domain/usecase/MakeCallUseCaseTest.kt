package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.NetworkConnectivityState
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.AudioCodec
import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.VoipSettings
import com.valevoip.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MakeCallUseCaseTest {

    private lateinit var fakeSipClient: FakeSipClient
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeNetworkConnectivityProvider: FakeNetworkConnectivityProvider
    private lateinit var makeCallUseCase: MakeCallUseCase

    @Before
    fun setUp() {
        fakeSipClient = FakeSipClient()
        fakeSettingsRepository = FakeSettingsRepository()
        fakeNetworkConnectivityProvider = FakeNetworkConnectivityProvider()

        makeCallUseCase = MakeCallUseCase(
            sipClient = fakeSipClient,
            settingsRepository = fakeSettingsRepository,
            networkConnectivityProvider = fakeNetworkConnectivityProvider
        )
    }

    @Test
    fun `should make call successfully when wifi only is disabled even on cellular`() {
        fakeSettingsRepository.currentSettings = VoipSettings(useWifiOnly = false)
        fakeNetworkConnectivityProvider.isWifiConnected = false

        val result = makeCallUseCase("1002")

        assertTrue(result.isSuccess)
        assertEquals("1002", fakeSipClient.lastCalledNumber)
    }

    @Test
    fun `should make call successfully to alphanumeric SIP username and trim whitespace`() {
        fakeSettingsRepository.currentSettings = VoipSettings(useWifiOnly = false)

        val result = makeCallUseCase("  alexandreskt16  ")

        assertTrue(result.isSuccess)
        assertEquals("alexandreskt16", fakeSipClient.lastCalledNumber)
    }

    @Test
    fun `should fail with IllegalArgumentException when target is blank`() {
        val result = makeCallUseCase("   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals(null, fakeSipClient.lastCalledNumber)
    }

    @Test
    fun `should make call successfully when wifi only is enabled and connected to wifi`() {
        fakeSettingsRepository.currentSettings = VoipSettings(useWifiOnly = true)
        fakeNetworkConnectivityProvider.isWifiConnected = true

        val result = makeCallUseCase("1002")

        assertTrue(result.isSuccess)
        assertEquals("1002", fakeSipClient.lastCalledNumber)
    }

    @Test
    fun `should block call when wifi only is enabled and not connected to wifi`() {
        fakeSettingsRepository.currentSettings = VoipSettings(useWifiOnly = true)
        fakeNetworkConnectivityProvider.isWifiConnected = false

        val result = makeCallUseCase("1002")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(null, fakeSipClient.lastCalledNumber)
    }

    @Test
    fun `should propagate error when sipClient fails to make call`() {
        fakeSettingsRepository.currentSettings = VoipSettings(useWifiOnly = false)
        fakeNetworkConnectivityProvider.isWifiConnected = true
        fakeSipClient.shouldFail = true

        val result = makeCallUseCase("1002")

        assertTrue(result.isFailure)
        assertEquals("SIP Call Failed", result.exceptionOrNull()?.message)
    }

    private class FakeSipClient : SipClient {
        var lastCalledNumber: String? = null
        var shouldFail: Boolean = false

        override val registrationState: StateFlow<SipRegistrationState> = MutableStateFlow(SipRegistrationState.None)
        override val isMuted: StateFlow<Boolean> = MutableStateFlow(false)
        override val isSpeakerEnabled: StateFlow<Boolean> = MutableStateFlow(false)
        override val availableAudioRoutes: StateFlow<List<AudioRoute>> = MutableStateFlow(emptyList())
        override val activeAudioRoute: StateFlow<AudioRoute?> = MutableStateFlow(null)
        override val availableAudioCodecs: StateFlow<List<AudioCodec>> = MutableStateFlow(emptyList())
        override val activeCallCodec: StateFlow<AudioCodec?> = MutableStateFlow(null)
        override suspend fun register(account: SipAccount): Flow<SipRegistrationState> = emptyFlow()
        override suspend fun unregister(): Flow<SipRegistrationState> = emptyFlow()

        override fun makeCall(number: String): Result<Unit> {
            return if (shouldFail) {
                Result.failure(Exception("SIP Call Failed"))
            } else {
                lastCalledNumber = number
                Result.success(Unit)
            }
        }

        override fun hangUp(): Result<Unit> = Result.success(Unit)
        override fun toggleMute(): Result<Unit> = Result.success(Unit)
        override fun toggleSpeaker(): Result<Boolean> = Result.success(true)
        override fun selectAudioRoute(routeId: String): Result<Unit> = Result.success(Unit)
        override fun setAudioCodecEnabled(codecId: String, enabled: Boolean): Result<Unit> = Result.success(Unit)
        override fun sendDtmf(digit: Char): Result<Unit> = Result.success(Unit)
        override fun toggleHold(): Result<Boolean> = Result.success(true)
        override fun muteCall(callId: String): Result<Unit> = Result.success(Unit)
        override fun acceptCall(): Result<Unit> = Result.success(Unit)
        override fun getCallStatusFlow(): Flow<CallStatus> = emptyFlow()
        override fun getCurrentCallNumber(): String? = null
        override fun getSynchronousCallStatus(): CallStatus = CallStatus.IDLE
        override fun getLinphoneVersion(): String = "5.0.0"
    }

    private class FakeSettingsRepository : SettingsRepository {
        override var currentSettings: VoipSettings = VoipSettings()
        override val settings: Flow<VoipSettings> = emptyFlow()
        override suspend fun setEchoCancellation(enabled: Boolean) {}
        override suspend fun setVibrate(enabled: Boolean) {}
        override suspend fun setWifiOnly(enabled: Boolean) {}
        override suspend fun setSrtpEnabled(enabled: Boolean) {}
    }

    private class FakeNetworkConnectivityProvider : NetworkConnectivityProvider {
        override var isWifiConnected: Boolean = true
        override var isConnected: Boolean = true
        override val connectivityState: Flow<NetworkConnectivityState> = emptyFlow()
    }
}
