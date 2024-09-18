package com.example.valevoip.domain.di

import com.example.valevoip.data.AccountRepository
import com.example.valevoip.data.SipAccountRepository
import com.example.valevoip.domain.usecase.AcceptUseCase
import com.example.valevoip.domain.usecase.GetCallStateUseCase
import com.example.valevoip.domain.usecase.GetAccountUseCase
import com.example.valevoip.domain.usecase.GetRegistrationStateUseCase
import com.example.valevoip.domain.usecase.InsertAccountUseCase
import com.example.valevoip.domain.usecase.RegisterAccountUseCase
import com.example.valevoip.domain.usecase.UnregisterAccountUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideRegisterAccountUseCase(
        sipAccountRepository: SipAccountRepository
    ): RegisterAccountUseCase {
        return RegisterAccountUseCase(repository = sipAccountRepository)
    }

    @Provides
    fun provideUnregisterAccountUseCase(
        sipAccountRepository: SipAccountRepository
    ): UnregisterAccountUseCase {
        return UnregisterAccountUseCase(repository = sipAccountRepository)
    }

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
    fun provideGetAccountUseCase(
        accountRepository: AccountRepository
    ): GetAccountUseCase {
        return GetAccountUseCase(accountRepository = accountRepository)
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