package com.example.valevoip.di

import com.example.data.local.AppDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.service.LinphoneManager
import com.example.data.service.LinphoneService
import com.example.domain.repository.ValeVoipService
import com.example.domain.repository.AccountRepository
import com.example.data.repository.AccountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DataModule {

    @Provides
    fun provideVoipService(linphoneManager: LinphoneManager): ValeVoipService {
        return LinphoneService(linphoneManager = linphoneManager)
    }

    @Provides
    fun provideConfigDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideConfigRepository(
        accountDao: AccountDao
    ): AccountRepository {
        return AccountRepositoryImpl(accountDao = accountDao)
    }
}