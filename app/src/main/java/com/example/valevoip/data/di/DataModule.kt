package com.example.valevoip.data.di

import com.example.valevoip.core.lib.linphone.LinphoneManagerInternal
import com.example.valevoip.data.SipAccountRepository
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
    fun provideSipAccountRepository(
        linphoneManager: LinphoneManagerInternal
    ): SipAccountRepository {
        return SipAccountRepositoryImpl(linphoneManager = linphoneManager)
    }
}