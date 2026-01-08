package com.example.domain.usecase

import com.example.domain.model.AccountModel
import com.example.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): AccountModel? = accountRepository.getAccount()
}