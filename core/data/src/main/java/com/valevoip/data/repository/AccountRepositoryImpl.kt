package com.valevoip.data.repository

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.repository.AccountRepository
import com.valevoip.data.security.SipAccountPrefs
import kotlinx.coroutines.flow.firstOrNull

internal class AccountRepositoryImpl(
    private val dataStore: DataStore<SipAccountPrefs>
) : AccountRepository {

    override suspend fun getAccount(): SipAccount? {
        val prefs = dataStore.data.firstOrNull() ?: return null

        if (prefs.username.isEmpty()) return null

        return SipAccount(
            username = prefs.username,
            password = prefs.password,
            domain = prefs.domain
        )
    }

    override suspend fun insertAccount(sipAccount: SipAccount) {
        dataStore.updateData { currentPrefs ->
            currentPrefs.copy(
                username = sipAccount.username,
                password = sipAccount.password,
                domain = sipAccount.domain
            )
        }
    }

    override suspend fun clearAccount() {
        dataStore.updateData { SipAccountPrefs() }
    }
}