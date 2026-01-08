package com.example.data.di

import com.example.data.local.dao.AccountDao
import com.example.data.repository.AccountRepositoryImpl
import com.example.data.service.LinphoneManager
import com.example.data.service.LinphoneService
import com.example.domain.repository.AccountRepository
import com.example.domain.repository.ValeVoipService
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
    fun provideVoipService(linphoneManager: LinphoneManager): ValeVoipService {
        return LinphoneService(linphoneManager)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(accountDao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(accountDao)
    }
}