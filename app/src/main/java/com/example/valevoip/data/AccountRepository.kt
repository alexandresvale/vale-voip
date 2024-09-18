package com.example.valevoip.data

import com.example.valevoip.domain.model.AccountModel

interface AccountRepository {
    suspend fun getAccount(): AccountModel?
    suspend fun insertAccount(accountModel: AccountModel)
}
