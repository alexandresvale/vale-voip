package com.valevoip.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.valevoip.data.security.CryptoSerializer
import com.valevoip.data.security.SipAccountPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSecureSipDataStore(
        @ApplicationContext context: Context,
        cryptoSerializer: CryptoSerializer,
    ): DataStore<SipAccountPrefs> {
        return DataStoreFactory.create(
            serializer = cryptoSerializer,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.dataStoreFile("secure_sip_account.json") }
        )
    }
}