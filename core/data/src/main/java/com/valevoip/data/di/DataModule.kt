package com.valevoip.data.di

import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.data.local.dao.AccountDao
import com.valevoip.data.repository.AccountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // O SipClient será provido pelo core:sip agora

    @Provides
    @Singleton
    fun provideAccountRepository(accountDao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(accountDao)
    }

}