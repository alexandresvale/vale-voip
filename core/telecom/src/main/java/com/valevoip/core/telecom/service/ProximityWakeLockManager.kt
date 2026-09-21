package com.valevoip.core.telecom.service

import android.content.Context
import android.os.PowerManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityWakeLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var cpuWakeLock: PowerManager.WakeLock? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    companion object {
        private const val WAKE_LOCK_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutos
        private const val TAG = "VALEVOIP_TELECOM"
    }

    fun acquire() {
        acquireProximityLock()
        acquireCpuLock()
    }

    fun release() {
        releaseLock(proximityWakeLock, "Proximity")
        proximityWakeLock = null
        releaseLock(cpuWakeLock, "CPU")
        cpuWakeLock = null
    }

    private fun acquireProximityLock() {
        if (proximityWakeLock == null &&
            powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)
        ) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "ValeVoip:ProximityLock"
            )
        }
        acquireLock(proximityWakeLock, "Proximity")
    }

    private fun acquireCpuLock() {
        if (cpuWakeLock == null) {
            cpuWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ValeVoip:CpuLock"
            )
        }
        acquireLock(cpuWakeLock, "CPU")
    }

    private fun acquireLock(wakeLock: PowerManager.WakeLock?, tag: String) {
        if (wakeLock?.isHeld == false) {
            try {
                wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
            } catch (e: Exception) {
                Log.e(TAG, "ProximityWakeLockManager | Falha ao adquirir $tag WakeLock", e)
            }
        }
    }

    private fun releaseLock(wakeLock: PowerManager.WakeLock?, tag: String) {
        if (wakeLock?.isHeld == true) {
            try {
                wakeLock.release()
            } catch (e: Exception) {
                Log.e(TAG, "ProximityWakeLockManager | Falha ao liberar $tag WakeLock", e)
            }
        }
    }
}
