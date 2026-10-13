package com.valevoip.data.repository

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipTransport
import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.data.local.datastore.SipAccountPrefs
import kotlinx.coroutines.flow.first

internal class AccountRepositoryImpl(
    private val dataStore: DataStore<SipAccountPrefs>
) : AccountRepository {

    override suspend fun getAccount(): SipAccount? {
        val prefs = dataStore.data.first()
        if (prefs.username.isEmpty()) return null

        return SipAccount(
            username = prefs.username,
            password = prefs.password,
            domain = prefs.domain,
            transport = SipTransport.valueOf(prefs.transport)
        )
    }

    override suspend fun insertAccount(sipAccount: SipAccount) {
        dataStore.updateData { prefs ->
            prefs.copy(
                username = sipAccount.username,
                password = sipAccount.password,
                domain = sipAccount.domain,
                transport = sipAccount.transport.name
            )
        }
    }

    override suspend fun clearAccount() {
        dataStore.updateData { SipAccountPrefs() }
    }
}