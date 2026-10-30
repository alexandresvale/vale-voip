package com.valevoip.data.local.datastore

import androidx.datastore.core.Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@Serializable
data class VoipSettingsPrefs(
    val echoCancellationEnabled: Boolean = true,
    val vibrateOnIncomingCall: Boolean = true,
    val useWifiOnly: Boolean = false
)

internal class VoipSettingsSerializer @Inject constructor() : Serializer<VoipSettingsPrefs> {
    override val defaultValue: VoipSettingsPrefs = VoipSettingsPrefs()

    override suspend fun readFrom(input: InputStream): VoipSettingsPrefs {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            Json.decodeFromString<VoipSettingsPrefs>(bytes.decodeToString())
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: VoipSettingsPrefs, output: OutputStream) {
        val bytes = Json.encodeToString(t).encodeToByteArray()
        output.write(bytes)
    }
}
