package com.valevoip.data.di

import com.valevoip.data.security.CryptoManager
import com.valevoip.data.security.TinkCryptoManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SecurityModule {
    @Binds
    abstract fun bindCryptoManager(impl: TinkCryptoManager): CryptoManager
}
