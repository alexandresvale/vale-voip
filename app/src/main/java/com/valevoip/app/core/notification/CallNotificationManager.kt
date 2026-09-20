package com.valevoip.app.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.valevoip.core.designsystem.R
import com.valevoip.app.core.service.CallService
import com.valevoip.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "voip_call_channel"
        const val SERVICE_NOTIFICATION_ID = 100
        const val CALL_NOTIFICATION_ID = 101
        const val ACTION_ANSWER = "ACTION_ANSWER"
        const val ACTION_HANGUP = "ACTION_HANGUP"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createChannel()
    }

    fun buildOnlineNotification(): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle("ValeVoip")
            .setContentText("Online")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun buildForegroundNotification(contactName: String, isIncoming: Boolean): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO_CALL", true)
            putExtra("CONTACT_NUMBER", contactName)
            putExtra("IS_INCOMING", isIncoming)
        }

        val pendingOpenApp = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle(if (isIncoming) "Chamada Recebida" else "Em Chamada")
            .setContentText(contactName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setAutoCancel(false)

        if (!isIncoming) {
            builder.setUsesChronometer(true)
        }

        addActions(builder, isIncoming)

        if (isIncoming) {
            builder.setFullScreenIntent(pendingOpenApp, true)
        }

        return builder.build()
    }

    fun cancelAll() {
        notificationManager?.cancel(CALL_NOTIFICATION_ID)
        notificationManager?.cancel(SERVICE_NOTIFICATION_ID)
    }

    private fun addActions(builder: NotificationCompat.Builder, isIncoming: Boolean) {
        val hangupIntent = Intent(context, CallService::class.java).apply {
            action = ACTION_HANGUP
        }
        val pendingHangup = PendingIntent.getService(
            context, 1, hangupIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val hangupText = if (isIncoming) "Rejeitar" else "Desligar"
        builder.addAction(0, hangupText, pendingHangup)

        if (isIncoming) {
            val answerIntent = Intent(context, CallService::class.java).apply {
                action = ACTION_ANSWER
            }
            val pendingAnswer = PendingIntent.getService(
                context, 2, answerIntent, PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Atender", pendingAnswer)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chamadas VoIP",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de chamadas ativas"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
}