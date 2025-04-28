package com.example.domain.usecase

import com.example.domain.repository.AccountRepository
import com.example.domain.model.AccountModel

class GetAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): AccountModel? = accountRepository.getAccount()
}