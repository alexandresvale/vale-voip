package com.valevoip.app.core.service

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallHardwareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var cpuWakeLock: PowerManager.WakeLock? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /**
     * Proximity Sensor (Apagar a tela ao encostar no ouvido)
     */
    fun activateSensors() {
        if (proximityWakeLock == null) {
            if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                proximityWakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "ValeVoip:ProximityLock"
                )
            }
        }

        if (cpuWakeLock == null) {
            cpuWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ValeVoip:CpuLock"
            )
        }

        activateLock(proximityWakeLock)
        activateLock(cpuWakeLock)
    }

    fun releaseSensors() {
        releaseLock(proximityWakeLock)
        releaseLock(cpuWakeLock)
    }

    private fun activateLock(wakeLock: PowerManager.WakeLock?) {
        if (wakeLock?.isHeld == false) {
            try {
                /* 30 minutos timeout de segurança */
                wakeLock.acquire(30 * 60 * 1000L)
            } catch (e: Exception) {
                // Log erro se necessário
            }
        }
    }

    private fun releaseLock(wakeLock: PowerManager.WakeLock?) {
        if (wakeLock?.isHeld == true) {
            try {
                wakeLock.release()
            } catch (e: Exception) {
                // Log erro
            }
        }
    }
}