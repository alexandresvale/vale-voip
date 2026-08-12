package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): SipAccount? = accountRepository.getAccount()
}