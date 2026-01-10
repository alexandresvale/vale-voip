package com.valevoip.data.repository

import com.valevoip.data.local.dao.AccountDao
import com.valevoip.data.local.mapper.toData
import com.valevoip.data.local.mapper.toDomain
import com.valevoip.domain.model.AccountModel
import com.valevoip.domain.repository.AccountRepository

internal class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun getAccount(): AccountModel? = accountDao.getAccount()?.toDomain()

    override suspend fun insertAccount(accountModel: AccountModel) = accountDao.insertAccount(accountModel.toData())

    override suspend fun clearAccount() = accountDao.clearAccount()
}