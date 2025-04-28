package com.example.valevoip.domain.di

import com.example.domain.repository.AccountRepository
import com.example.valevoip.data.SipAccountRepository
import com.example.valevoip.domain.usecase.AcceptUseCase
import com.example.domain.usecase.GetAccountUseCase
import com.example.valevoip.domain.usecase.GetCallStateUseCase
import com.example.valevoip.domain.usecase.GetRegistrationStateUseCase
import com.example.valevoip.domain.usecase.InsertAccountUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideAcceptUseCaseUseCase(
        sipAccountRepository: SipAccountRepository
    ): AcceptUseCase {
        return AcceptUseCase(repository = sipAccountRepository)
    }

    @Provides
    fun provideInsertAccountUseCase(
        accountRepository: AccountRepository
    ): InsertAccountUseCase {
        return InsertAccountUseCase(accountRepository = accountRepository)
    }

    @Provides
    fun provideGetRegistrationStateUseCase(
        sipAccountRepository: SipAccountRepository
    ): GetRegistrationStateUseCase {
        return GetRegistrationStateUseCase(repository = sipAccountRepository)
    }

    @Provides
    fun provideGetCallStateUseCase(
        sipAccountRepository: SipAccountRepository
    ): GetCallStateUseCase {
        return GetCallStateUseCase(repository = sipAccountRepository)
    }
}