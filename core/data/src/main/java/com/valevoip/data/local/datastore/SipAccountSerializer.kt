package com.valevoip.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.valevoip.data.security.CryptoManager
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.inject.Inject

internal class SipAccountSerializer @Inject constructor(
    private val cryptoManager: CryptoManager
) : Serializer<SipAccountPrefs> {
    override val defaultValue: SipAccountPrefs = SipAccountPrefs()

    override suspend fun readFrom(input: InputStream): SipAccountPrefs {
        val ciphertext = input.readBytes()
        if (ciphertext.isEmpty()) return defaultValue

        return try {
            val plaintext = cryptoManager.decrypt(ciphertext, ASSOCIATED_DATA)
            Json.decodeFromString<SipAccountPrefs>(plaintext.decodeToString())
        } catch (e: GeneralSecurityException) {
            throw CorruptionException("Não foi possível descriptografar a conta SIP.", e)
        } catch (e: SerializationException) {
            throw CorruptionException("Conteúdo da conta SIP inválido.", e)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: SipAccountPrefs, output: OutputStream) {
        val plaintext = Json.encodeToString(t).encodeToByteArray()
        output.write(cryptoManager.encrypt(plaintext, ASSOCIATED_DATA))
    }

    private companion object {
        val ASSOCIATED_DATA = "secure_sip_account".encodeToByteArray()
    }
}
