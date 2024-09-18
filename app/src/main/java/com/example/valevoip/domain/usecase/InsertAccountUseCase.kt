package com.example.valevoip.domain.usecase

import com.example.valevoip.data.AccountRepository
import com.example.valevoip.domain.model.AccountModel

class InsertAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountModel: AccountModel) = accountRepository.insertAccount(accountModel)
}