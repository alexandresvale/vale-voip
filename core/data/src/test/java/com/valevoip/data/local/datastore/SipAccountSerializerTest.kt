package com.valevoip.data.local.datastore

import com.valevoip.data.security.CryptoManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal class SipAccountSerializerTest {

    private val cryptoManager = object : CryptoManager {
        override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray = plaintext

        override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray = ciphertext
    }

    private val serializer = SipAccountSerializer(cryptoManager)

    @Test
    fun `empty file returns default account`(): Unit = runTest {
        val result = serializer.readFrom(ByteArrayInputStream(byteArrayOf()))

        assertEquals(SipAccountPrefs(), result)
    }

    @Test
    fun `write and read preserves account fields`() = runTest {
        val account = SipAccountPrefs(
            username = "test-user",
            password = "test-password",
            domain = "sip.example.test",
            transport = "TLS"
        )
        val output = ByteArrayOutputStream()

        serializer.writeTo(account, output)
        val result = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(account, result)
    }
}