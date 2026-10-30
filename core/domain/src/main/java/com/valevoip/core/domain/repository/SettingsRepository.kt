package com.valevoip.core.domain.repository

import com.valevoip.core.domain.model.VoipSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<VoipSettings>
    suspend fun setEchoCancellation(enabled: Boolean)
    suspend fun setVibrate(enabled: Boolean)
    suspend fun setWifiOnly(enabled: Boolean)
}
