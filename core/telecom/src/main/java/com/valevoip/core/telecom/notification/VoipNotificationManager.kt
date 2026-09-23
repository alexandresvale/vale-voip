package com.valevoip.core.telecom.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.valevoip.core.designsystem.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Gerencia todas as notificações do VoIP.
 * Usa Deep Links (valevoip://) para abrir o app sem acoplamento com a MainActivity.
 */
class VoipNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SERVICE_CHANNEL_ID = "voip_service_channel"
        const val CALL_CHANNEL_ID = "voip_call_channel"
        const val SERVICE_NOTIFICATION_ID = 100
        const val CALL_NOTIFICATION_ID = 101

        const val ACTION_ANSWER = "ACTION_ANSWER"
        const val ACTION_HANGUP = "ACTION_HANGUP"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    // --- Notificação de Serviço (Online) ---

    fun buildOnlineNotification(): Notification {
        val openAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse("valevoip://home")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle("ValeVoip")
            .setContentText("Online")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    // --- Notificação de Chamada ---

    fun buildCallNotification(
        contactName: String,
        isIncoming: Boolean,
        serviceClass: Class<*>
    ): Notification {
        val uriString = if (contactName.isNotBlank()) {
            "valevoip://call/${Uri.encode(contactName)}"
        } else {
            "valevoip://call"
        }
        val openAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingOpenApp = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
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

        addCallActions(builder, isIncoming, serviceClass)

        return builder.build()
    }

    fun showCallNotification(contactName: String, isIncoming: Boolean, serviceClass: Class<*>) {
        val notification = buildCallNotification(contactName, isIncoming, serviceClass)
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    fun dismissCallNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    fun cancelAll() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        notificationManager.cancel(SERVICE_NOTIFICATION_ID)
    }

    // --- Privados ---

    private fun addCallActions(
        builder: NotificationCompat.Builder,
        isIncoming: Boolean,
        serviceClass: Class<*>
    ) {
        val hangupIntent = Intent(context, serviceClass).apply {
            action = ACTION_HANGUP
        }
        val pendingHangup = PendingIntent.getService(
            context, 1, hangupIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val hangupText = if (isIncoming) "Rejeitar" else "Desligar"
        builder.addAction(0, hangupText, pendingHangup)

        if (isIncoming) {
            val answerIntent = Intent(context, serviceClass).apply {
                action = ACTION_ANSWER
            }
            val pendingAnswer = PendingIntent.getService(
                context, 2, answerIntent, PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Atender", pendingAnswer)
        }
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Status do Serviço VoIP",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Indica que o serviço VoIP está ativo"
            }

            val callChannel = NotificationChannel(
                CALL_CHANNEL_ID,
                "Chamadas VoIP",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de chamadas recebidas e ativas"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(callChannel)
        }
    }
}
