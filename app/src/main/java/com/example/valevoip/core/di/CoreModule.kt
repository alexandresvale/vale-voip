package com.example.valevoip.core.di

import android.app.Application
import com.example.valevoip.core.lib.linphone.LinphoneManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideLinphoneManager(application: Application): LinphoneManager {
        return LinphoneManager(context = application)
    }

}