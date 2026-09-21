package com.valevoip.core.telecom.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.valevoip.core.telecom.service.VoipForegroundService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("VALEVOIP_TELECOM", "BootCompletedReceiver | Sistema reiniciado. Iniciando VoipForegroundService...")

            val serviceIntent = Intent(context, VoipForegroundService::class.java).apply {
                action = VoipForegroundService.ACTION_START_MONITORING
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
