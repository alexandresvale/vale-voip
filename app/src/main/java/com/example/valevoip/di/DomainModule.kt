package com.example.valevoip.di

import com.example.domain.repository.AccountRepository
import com.example.domain.repository.ValeVoipService
import com.example.domain.usecase.GetAccountUseCase
import com.example.domain.usecase.RegisterUserUseCase
import com.example.domain.usecase.UnregisterAccountUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    @Provides
    fun provideRegisterUserUseCase(
        valeVoipService: ValeVoipService,
        repository: AccountRepository
    ): RegisterUserUseCase {
        return RegisterUserUseCase(valeVoipService = valeVoipService, repository = repository)
    }

    @Provides
    fun provideUnregisterAccountUseCase(
        valeVoipService: ValeVoipService,
        repository: AccountRepository
    ): UnregisterAccountUseCase {
        return UnregisterAccountUseCase(valeVoipService = valeVoipService, repository = repository)
    }

    @Provides
    fun provideGetAccountUseCase(
        accountRepository: AccountRepository
    ): GetAccountUseCase {
        return GetAccountUseCase(accountRepository = accountRepository)
    }
}