package com.valevoip.core.telecom.service

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerencia exclusivamente o sensor de proximidade (PROXIMITY_SCREEN_OFF_WAKE_LOCK) durante chamadas.
 * Os WakeLocks de CPU (PARTIAL_WAKE_LOCK) e Wi-Fi já são gerenciados nativamente pelo
 * AndroidPlatformHelper da SDK Linphone.
 */
@Singleton
class ProximityWakeLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    companion object {
        private const val WAKE_LOCK_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutos
    }

    fun setProximitySensorEnabled(enabled: Boolean) {
        if (enabled) {
            acquireProximityLock()
        } else {
            release()
        }
    }

    fun release() {
        val lock = proximityWakeLock
        if (lock?.isHeld == true) {
            try {
                lock.release()
                Timber.d("ProximityWakeLock liberado")
            } catch (e: Exception) {
                Timber.e(e, "Falha ao liberar ProximityWakeLock")
            }
        }
        proximityWakeLock = null
    }

    private fun acquireProximityLock() {
        if (!powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            return
        }
        if (proximityWakeLock == null) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "ValeVoip:ProximityLock"
            ).apply {
                setReferenceCounted(false)
            }
        }
        val lock = proximityWakeLock
        if (lock?.isHeld == false) {
            try {
                lock.acquire(WAKE_LOCK_TIMEOUT_MS)
                Timber.d("ProximityWakeLock adquirido")
            } catch (e: Exception) {
                Timber.e(e, "Falha ao adquirir ProximityWakeLock")
            }
        }
    }
}
