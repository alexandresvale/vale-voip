package com.valevoip.data.repository

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.model.VoipSettings
import com.valevoip.core.domain.repository.SettingsRepository
import com.valevoip.data.local.datastore.VoipSettingsPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<VoipSettingsPrefs>
) : SettingsRepository {

    override val settings: Flow<VoipSettings> = dataStore.data.map { prefs ->
        VoipSettings(
            echoCancellationEnabled = prefs.echoCancellationEnabled,
            vibrateOnIncomingCall = prefs.vibrateOnIncomingCall,
            useWifiOnly = prefs.useWifiOnly
        )
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
