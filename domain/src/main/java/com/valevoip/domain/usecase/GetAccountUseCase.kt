package com.valevoip.domain.usecase

import com.valevoip.domain.model.AccountModel
import com.valevoip.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): AccountModel? = accountRepository.getAccount()
}