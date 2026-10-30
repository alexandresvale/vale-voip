package com.valevoip.data.repository

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.model.VoipSettings
import com.valevoip.core.domain.repository.SettingsRepository
import com.valevoip.data.local.datastore.VoipSettingsPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<VoipSettingsPrefs>
) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var _currentSettings: VoipSettings = VoipSettings()

    override val currentSettings: VoipSettings
        get() = _currentSettings

    override val settings: Flow<VoipSettings> = dataStore.data.map { prefs ->
        VoipSettings(
            echoCancellationEnabled = prefs.echoCancellationEnabled,
            vibrateOnIncomingCall = prefs.vibrateOnIncomingCall,
            useWifiOnly = prefs.useWifiOnly
        )
    }

    init {
        settings
            .onEach { _currentSettings = it }
            .launchIn(scope)
    }

    override suspend fun setEchoCancellation(enabled: Boolean) {
        dataStore.updateData { it.copy(echoCancellationEnabled = enabled) }
    }

    override suspend fun setVibrate(enabled: Boolean) {
        dataStore.updateData { it.copy(vibrateOnIncomingCall = enabled) }
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        dataStore.updateData { it.copy(useWifiOnly = enabled) }
    }
}
