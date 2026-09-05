package com.valevoip.data.repository

import com.valevoip.data.local.dao.AccountDao
import com.valevoip.data.local.mapper.toData
import com.valevoip.data.local.mapper.toDomain
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.repository.AccountRepository

internal class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun getAccount(): SipAccount? = accountDao.getAccount()?.toDomain()

    override suspend fun insertAccount(sipAccount: SipAccount) = accountDao.insertAccount(sipAccount.toData())

    override suspend fun clearAccount() = accountDao.clearAccount()
}