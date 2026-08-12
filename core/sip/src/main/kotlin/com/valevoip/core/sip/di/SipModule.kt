package com.valevoip.core.sip.di

import android.content.Context
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.sip.LinphoneSipClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SipModule {

    @Provides
    @Singleton
    fun provideSipClient(
        @ApplicationContext context: Context
    ): SipClient {
        return LinphoneSipClient(context)
    }
}
