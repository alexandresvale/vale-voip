package com.valevoip.core.sip.di

import android.content.Context
import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.core.domain.repository.CallHistoryRepository
import com.valevoip.core.domain.repository.SettingsRepository
import com.valevoip.core.sip.LinphoneSipClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SipModule {

    @Provides
    @Singleton
    fun provideSipClient(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository,
        networkConnectivityProvider: NetworkConnectivityProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SipClient {
        return LinphoneSipClient(
            context = context,
            settingsRepository = settingsRepository,
            networkConnectivityProvider = networkConnectivityProvider,
            ioDispatcher = ioDispatcher
        )
    }

    @Provides
    fun provideCallHistoryRepository(sipClient: SipClient): CallHistoryRepository {
        return sipClient as? CallHistoryRepository
            ?: throw IllegalStateException("A instância de SipClient deve implementar CallHistoryRepository")
    }
}
