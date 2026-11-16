package com.valevoip.core.telecom.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.valevoip.core.designsystem.R
import com.valevoip.core.domain.model.SipRegistrationState
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

    // --- Notificação Persistente de Status do Serviço SIP ---

    fun buildServiceStatusNotification(
        username: String?,
        state: SipRegistrationState
    ): Notification {
        val openAppIntent = Intent(Intent.ACTION_VIEW, "valevoip://home".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusLabel = when (state) {
            SipRegistrationState.Ok -> "Conectado"
            SipRegistrationState.Progress,
            SipRegistrationState.Refreshing -> "Conectando ao servidor SIP..."
            is SipRegistrationState.Failed -> "Falha na conexão SIP"
            SipRegistrationState.Cleared,
            SipRegistrationState.None -> "Desconectado"
        }

        val contentText = if (!username.isNullOrBlank()) {
            "Ramal $username • $statusLabel"
        } else {
            statusLabel
        }

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle("Vale VoIP")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun updateServiceStatusNotification(
        username: String?,
        state: SipRegistrationState
    ) {
        val notification = buildServiceStatusNotification(username = username, state = state)
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification)
    }

    // --- Notificação de Chamada ---

    fun buildCallNotification(
        contactName: String,
        isIncoming: Boolean,
        serviceClass: Class<*>,
        isOnHold: Boolean = false
    ): Notification {
        val uriString = if (contactName.isNotBlank()) {
            "valevoip://call?number=${Uri.encode(contactName)}"
        } else {
            "valevoip://call"
        }
        val openAppIntent = Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingOpenApp = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val displayContact = contactName.ifBlank { "Número desconhecido" }
        val statusTitle = when {
            isIncoming -> "Chamada Recebida"
            isOnHold -> "Chamada em Espera"
            else -> "Em Chamada"
        }

        val builder = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle(statusTitle)
            .setContentText(displayContact)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setAutoCancel(false)

        if (!isIncoming && !isOnHold) {
            builder.setUsesChronometer(true)
        }

        addCallActions(builder, isIncoming, serviceClass)

        return builder.build()
    }

    fun showCallNotification(
        contactName: String,
        isIncoming: Boolean,
        serviceClass: Class<*>,
        isOnHold: Boolean = false
    ) {
        val notification = buildCallNotification(
            contactName = contactName,
            isIncoming = isIncoming,
            serviceClass = serviceClass,
            isOnHold = isOnHold
        )
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
            context,
            1,
            hangupIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val hangupText = if (isIncoming) "Rejeitar" else "Desligar"
        builder.addAction(0, hangupText, pendingHangup)

        if (isIncoming) {
            val answerIntent = Intent(context, serviceClass).apply {
                action = ACTION_ANSWER
            }
            val pendingAnswer = PendingIntent.getService(
                context,
                2,
                answerIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Atender", pendingAnswer)
        }
    }

    private fun createChannels() {
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Status do Serviço VoIP",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Indica o estado real da conexão SIP VoIP em segundo plano"
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
