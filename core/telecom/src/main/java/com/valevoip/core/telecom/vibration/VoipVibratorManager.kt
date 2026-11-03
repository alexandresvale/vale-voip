package com.valevoip.core.telecom.vibration

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador responsável pelo ciclo de vibração tátil de chamadas VoIP.
 * Compatível com Android 8.0 (API 26) até Android 14+ (API 34+).
 */
@Singleton
class VoipVibratorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    @Volatile
    private var isVibrating: Boolean = false

    // Padrão de vibração: espera 0ms, vibra 1000ms, pausa 1000ms
    private val ringVibrationPattern = longArrayOf(0L, 1000L, 1000L)

    /**
     * Inicia a vibração contínua de toque para chamada recebida.
     * Repete indefinidamente até que [stopVibration] seja invocado.
     */
    fun startRingingVibration() {
        val vib = vibrator ?: run {
            Timber.w("Vibrator não disponível no dispositivo")
            return
        }

        if (!vib.hasVibrator()) {
            Timber.d("Dispositivo não possui hardware de vibração")
            return
        }

        if (isVibrating) {
            Timber.d("Vibração de chamada já em execução")
            return
        }

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            // Índice 0 indica repetição contínua a partir do início do padrão
            val effect = VibrationEffect.createWaveform(ringVibrationPattern, 0)
            vib.vibrate(effect, audioAttributes)
            isVibrating = true
            Timber.d("Vibração de chamada recebida iniciada")
        } catch (e: Exception) {
            Timber.e(e, "Falha ao iniciar vibração de chamada")
        }
    }

    /**
     * Interrompe imediatamente qualquer vibração ativa.
     */
    fun stopVibration() {
        if (!isVibrating) return
        try {
            vibrator?.cancel()
            Timber.d("Vibração de chamada interrompida")
        } catch (e: Exception) {
            Timber.e(e, "Falha ao cancelar vibração")
        } finally {
            isVibrating = false
        }
    }

    fun isVibrating(): Boolean = isVibrating
}
