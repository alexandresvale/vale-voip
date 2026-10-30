package com.valevoip.data.di

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.client.DeviceInfoProvider
import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.core.domain.repository.SettingsRepository
import com.valevoip.data.device.AndroidDeviceInfoProvider
import com.valevoip.data.local.datastore.SipAccountPrefs
import com.valevoip.data.local.datastore.VoipSettingsPrefs
import com.valevoip.data.repository.AccountRepositoryImpl
import com.valevoip.data.repository.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAccountRepository(dataStore: DataStore<SipAccountPrefs>): AccountRepository {
        return AccountRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<VoipSettingsPrefs>): SettingsRepository {
        return SettingsRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideDeviceInfoProvider(impl: AndroidDeviceInfoProvider): DeviceInfoProvider {
        return impl
    }
}
