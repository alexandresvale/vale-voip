package com.example.valevoip.data.repository

import com.example.valevoip.data.AccountRepository
import com.example.valevoip.data.datasource.local.dao.AccountDao
import com.example.valevoip.data.datasource.local.mapper.toData
import com.example.valevoip.data.datasource.local.mapper.toDomain
import com.example.valevoip.domain.model.AccountModel

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun getAccount(): AccountModel? = accountDao.getAccount()?.toDomain()

    override suspend fun insertAccount(accountModel: AccountModel) = accountDao.insertAccount(accountModel.toData())
}