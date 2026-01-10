package com.valevoip.domain.repository

import com.valevoip.domain.model.AccountModel

interface AccountRepository {
    suspend fun getAccount(): AccountModel?
    suspend fun insertAccount(accountModel: AccountModel)
    suspend fun clearAccount()
}
