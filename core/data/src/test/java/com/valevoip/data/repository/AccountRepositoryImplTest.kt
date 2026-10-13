package com.valevoip.data.repository

import androidx.datastore.core.DataStore
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipTransport
import com.valevoip.data.local.datastore.SipAccountPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class AccountRepositoryImplTest {

    @Test
    fun `getAccount returns saved account`() = runTest {
        val prefs = SipAccountPrefs(
            username = "test-user",
            password = "test-password",
            domain = "sip.example.test",
            transport = "TLS"
        )
        val dataStore = mockk<DataStore<SipAccountPrefs>> {
            every { data } returns flowOf(prefs)
        }
        val repository = AccountRepositoryImpl(dataStore)

        val result = repository.getAccount()

        assertEquals(
            SipAccount(
                username = "test-user",
                password = "test-password",
                domain = "sip.example.test",
                transport = SipTransport.TLS
            ),
            result
        )
    }

    @Test
    fun `insertAccount writes all account fields`() = runTest {
        val dataStore = mockk<DataStore<SipAccountPrefs>>()
        var writtenPrefs: SipAccountPrefs? = null

        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform =
                firstArg<suspend (SipAccountPrefs) -> SipAccountPrefs>()
            transform(SipAccountPrefs()).also { writtenPrefs = it }
        }

        val repository = AccountRepositoryImpl(dataStore)
        val account = SipAccount(
            username = "test-user",
            password = "test-password",
            domain = "sip.example.test",
            transport = SipTransport.TLS
        )

        repository.insertAccount(account)

        assertEquals(
            SipAccountPrefs(
                username = account.username,
                password = account.password,
                domain = account.domain,
                transport = account.transport.name
            ),
            writtenPrefs
        )
    }

    @Test
    fun `clearAccount writes default preferences`() = runTest {
        val dataStore = mockk<DataStore<SipAccountPrefs>>()
        var writtenPrefs: SipAccountPrefs? = null

        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform =
                firstArg<suspend (SipAccountPrefs) -> SipAccountPrefs>()
            transform(
                SipAccountPrefs(
                    username = "existing-user",
                    password = "existing-password",
                    domain = "sip.example.test",
                    transport = "TLS"
                )
            ).also { writtenPrefs = it }
        }

        val repository = AccountRepositoryImpl(dataStore)

        repository.clearAccount()

        assertEquals(SipAccountPrefs(), writtenPrefs)
    }
}