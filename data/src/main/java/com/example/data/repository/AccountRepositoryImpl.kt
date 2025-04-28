package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.mapper.toData
import com.example.data.local.mapper.toDomain
import com.example.domain.model.AccountModel
import com.example.domain.repository.AccountRepository

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun getAccount(): AccountModel? = accountDao.getAccount()?.toDomain()

    override suspend fun insertAccount(accountModel: AccountModel) = accountDao.insertAccount(accountModel.toData())

    override suspend fun clearAccount() = accountDao.clearAccount()
}