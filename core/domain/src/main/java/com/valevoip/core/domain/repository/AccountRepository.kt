package com.valevoip.core.domain.repository

import com.valevoip.core.domain.model.SipAccount

interface AccountRepository {
    suspend fun getAccount(): SipAccount?
    suspend fun insertAccount(sipAccount: SipAccount)
    suspend fun clearAccount()
}
