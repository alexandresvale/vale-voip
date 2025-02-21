package com.example.valevoip.di

import com.example.domain.repository.AccountRepository
import com.example.domain.repository.ValeVoipService
import com.example.domain.usecase.RegisterUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    @Provides
    fun provideRegisterUserUseCase(
        service: ValeVoipService,
        repository: AccountRepository
    ): RegisterUserUseCase {
        return RegisterUserUseCase(voipService = service, repository = repository)
    }
}