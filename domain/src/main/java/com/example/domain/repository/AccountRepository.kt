package com.example.domain.repository

import com.example.domain.model.AccountModel

interface AccountRepository {
    suspend fun getAccount(): AccountModel?
    suspend fun insertAccount(accountModel: AccountModel)
    suspend fun clearAccount()
}
