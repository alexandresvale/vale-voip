package com.valevoip.data.di

import com.valevoip.data.local.dao.AccountDao
import com.valevoip.data.repository.AccountRepositoryImpl
import com.valevoip.data.service.LinphoneManager
import com.valevoip.data.service.SipClientImpl
import com.valevoip.domain.repository.AccountRepository
import com.valevoip.domain.repository.SipClient
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
    fun provideVoipService(linphoneManager: LinphoneManager): SipClient {
        return SipClientImpl(linphoneManager)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(accountDao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(accountDao)
    }
}