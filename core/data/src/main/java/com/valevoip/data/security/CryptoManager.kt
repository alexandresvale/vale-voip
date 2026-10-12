package com.valevoip.data.security

import java.security.GeneralSecurityException

interface CryptoManager {
    fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray

    @Throws(GeneralSecurityException::class)
    fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray
}
