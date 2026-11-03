package com.valevoip.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.valevoip.core.domain.di.IoDispatcher
import com.valevoip.data.local.datastore.SipAccountPrefs
import com.valevoip.data.local.datastore.SipAccountSerializer
import com.valevoip.data.local.datastore.VoipSettingsPrefs
import com.valevoip.data.local.datastore.VoipSettingsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {
    private const val SIP_ACCOUNT_FILE = "secure_sip_account.pb"
    private const val VOIP_SETTINGS_FILE = "voip_settings.json"
    private const val TAG = "DataStoreModule"

    @Provides
    @Singleton
    fun provideSipAccountDataStore(
        @ApplicationContext context: Context,
        serializer: SipAccountSerializer,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): DataStore<SipAccountPrefs> = DataStoreFactory.create(
        serializer = serializer,
        corruptionHandler = ReplaceFileCorruptionHandler { exception ->
            Timber.e(exception, "SIP account store corrupted; resetting.")
            SipAccountPrefs()
        },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.dataStoreFile(SIP_ACCOUNT_FILE) }
    )

    @Provides
    @Singleton
    fun provideVoipSettingsDataStore(
        @ApplicationContext context: Context,
        serializer: VoipSettingsSerializer,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): DataStore<VoipSettingsPrefs> = DataStoreFactory.create(
        serializer = serializer,
        corruptionHandler = ReplaceFileCorruptionHandler { exception ->
            Timber.e(exception, "VoIP settings store corrupted; resetting.")
            VoipSettingsPrefs()
        },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.dataStoreFile(VOIP_SETTINGS_FILE) }
    )
}
