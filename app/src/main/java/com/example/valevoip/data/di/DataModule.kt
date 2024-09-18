package com.example.valevoip.data.di

import android.app.Application
import androidx.room.Room
import com.example.valevoip.core.lib.linphone.LinphoneManager
import com.example.valevoip.data.AccountRepository
import com.example.valevoip.data.SipAccountRepository
import com.example.valevoip.data.datasource.local.AppDatabase
import com.example.valevoip.data.datasource.local.dao.AccountDao
import com.example.valevoip.data.repository.AccountRepositoryImpl
import com.example.valevoip.data.repository.SipAccountRepositoryImpl
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
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "vale_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideConfigDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    @Singleton
    fun provideSipAccountRepository(
        linphoneManager: LinphoneManager
    ): SipAccountRepository {
        return SipAccountRepositoryImpl(linphoneManager = linphoneManager)
    }

    @Provides
    @Singleton
    fun provideConfigRepository(
        accountDao: AccountDao
    ): AccountRepository {
        return AccountRepositoryImpl(accountDao = accountDao)
    }
}