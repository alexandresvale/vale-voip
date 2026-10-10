package com.valevoip.data.security

import android.content.Context
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

import com.valevoip.data.di.IoDispatcher

@Singleton
class CryptoSerializer @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : Serializer<SipAccountPrefs> {
    override val defaultValue: SipAccountPrefs = SipAccountPrefs()
    private val aead: Aead

    init {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://sip_master_key")
            .build()
            .keysetHandle
        aead = keysetHandle.getPrimitive(Aead::class.java)
    }


    override suspend fun readFrom(input: InputStream): SipAccountPrefs = withContext(dispatcher) {
        try {
            val encryptedBytes = input.readBytes()
            if (encryptedBytes.isEmpty()) return@withContext defaultValue

            val decryptedBytes = aead.decrypt(encryptedBytes, null)
            val json = String(decryptedBytes)
            Json.decodeFromString<SipAccountPrefs>(json)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SipAccountPrefs, output: OutputStream) = withContext(dispatcher) {
        val json = Json.encodeToString<SipAccountPrefs>(t)
        val encryptedBytes = aead.encrypt(json.toByteArray(), null)
        output.write(encryptedBytes)
    }
}