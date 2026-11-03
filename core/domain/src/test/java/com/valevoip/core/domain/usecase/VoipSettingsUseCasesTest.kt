package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.VoipSettings
import com.valevoip.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VoipSettingsUseCasesTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var setEchoCancellationUseCase: SetEchoCancellationUseCase
    private lateinit var setVibrateOnCallUseCase: SetVibrateOnCallUseCase
    private lateinit var setWifiOnlyUseCase: SetWifiOnlyUseCase
    private lateinit var observeVoipSettingsUseCase: ObserveVoipSettingsUseCase

    @Before
    fun setUp() {
        fakeSettingsRepository = FakeSettingsRepository()
        setEchoCancellationUseCase = SetEchoCancellationUseCase(fakeSettingsRepository)
        setVibrateOnCallUseCase = SetVibrateOnCallUseCase(fakeSettingsRepository)
        setWifiOnlyUseCase = SetWifiOnlyUseCase(fakeSettingsRepository)
        observeVoipSettingsUseCase = ObserveVoipSettingsUseCase(fakeSettingsRepository)
    }

    @Test
    fun `setEchoCancellation updates repository value`() = runTest {
        setEchoCancellationUseCase(false)
        assertFalse(fakeSettingsRepository.currentSettings.echoCancellationEnabled)

        setEchoCancellationUseCase(true)
        assertTrue(fakeSettingsRepository.currentSettings.echoCancellationEnabled)
    }

    @Test
    fun `setVibrateOnCall updates repository value`() = runTest {
        setVibrateOnCallUseCase(false)
        assertFalse(fakeSettingsRepository.currentSettings.vibrateOnIncomingCall)

        setVibrateOnCallUseCase(true)
        assertTrue(fakeSettingsRepository.currentSettings.vibrateOnIncomingCall)
    }

    @Test
    fun `setWifiOnly updates repository value`() = runTest {
        setWifiOnlyUseCase(true)
        assertTrue(fakeSettingsRepository.currentSettings.useWifiOnly)

        setWifiOnlyUseCase(false)
        assertFalse(fakeSettingsRepository.currentSettings.useWifiOnly)
    }

    @Test
    fun `observeVoipSettings emits latest settings from repository`() = runTest {
        val initialSettings = observeVoipSettingsUseCase().first()
        assertEquals(VoipSettings(), initialSettings)

        setWifiOnlyUseCase(true)
        val updated = observeVoipSettingsUseCase().first()
        assertTrue(updated.useWifiOnly)
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val _settingsFlow = MutableStateFlow(VoipSettings())

        override var currentSettings: VoipSettings
            get() = _settingsFlow.value
            set(value) {
                _settingsFlow.value = value
            }

        override val settings: Flow<VoipSettings> = _settingsFlow

        override suspend fun setEchoCancellation(enabled: Boolean) {
            currentSettings = currentSettings.copy(echoCancellationEnabled = enabled)
        }

        override suspend fun setVibrate(enabled: Boolean) {
            currentSettings = currentSettings.copy(vibrateOnIncomingCall = enabled)
        }

        override suspend fun setWifiOnly(enabled: Boolean) {
            currentSettings = currentSettings.copy(useWifiOnly = enabled)
        }
    }
}
