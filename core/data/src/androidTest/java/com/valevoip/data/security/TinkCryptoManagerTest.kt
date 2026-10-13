package com.valevoip.data.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.security.GeneralSecurityException

@RunWith(AndroidJUnit4::class)
internal class TinkCryptoManagerTest {
    @Test
    fun encrypted_data_decrypts_to_original_plaintext() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val cryptoManager = TinkCryptoManager(testContext)
        val plaintext = "test-only-secret".encodeToByteArray()
        val associatedData = "test-associated-data".encodeToByteArray()

        val ciphertext = cryptoManager.encrypt(plaintext, associatedData)
        val decrypted = cryptoManager.decrypt(ciphertext, associatedData)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun modified_ciphertext_fails_authentication() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val cryptoManager = TinkCryptoManager(testContext)
        val plaintext = "test-only-secret".encodeToByteArray()
        val associatedData = "test-associated-data".encodeToByteArray()
        val ciphertext = cryptoManager.encrypt(plaintext, associatedData)
        val modifiedCiphertext = ciphertext.copyOf().apply {
            val lastIndex = lastIndex
            this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte()
        }

        assertThrows(GeneralSecurityException::class.java) {
            cryptoManager.decrypt(modifiedCiphertext, associatedData)
        }
    }

    @Test
    fun wrong_associated_data_fails_authentication() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val cryptoManager = TinkCryptoManager(testContext)
        val ciphertext = cryptoManager.encrypt(
            "test-only-secret".encodeToByteArray(),
            "correct-context".encodeToByteArray()
        )

        assertThrows(GeneralSecurityException::class.java) {
            cryptoManager.decrypt(
                ciphertext,
                "different-context".encodeToByteArray()
            )
        }
    }
}