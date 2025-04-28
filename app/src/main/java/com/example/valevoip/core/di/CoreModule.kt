package com.example.valevoip.core.di

import android.app.Application
import com.example.valevoip.core.lib.linphone.LinphoneManagerInternal
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
    fun provideLinphoneManager(application: Application): LinphoneManagerInternal {
        return LinphoneManagerInternal(context = application)
    }

}