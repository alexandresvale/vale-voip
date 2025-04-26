package com.example.valevoip.domain.usecase

import com.example.domain.repository.AccountRepository
import com.example.domain.model.AccountModel

class InsertAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountModel: AccountModel) = accountRepository.insertAccount(accountModel)
}