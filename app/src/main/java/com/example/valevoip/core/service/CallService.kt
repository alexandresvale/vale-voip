package com.example.valevoip.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.valevoip.R
import com.example.valevoip.core.lib.linphone.LinphoneManagerInternal
import com.example.valevoip.main.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.RegistrationState
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var linphoneManager: LinphoneManagerInternal

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())


    override fun onCreate() {
        super.onCreate()
        observeLinphoneEvents()
        logEvent("Service iniciado = ${linphoneManager.core.version}")
    }

    override fun onDestroy() {
        linphoneManager.stop()
        super.onDestroy()
        logEvent("Service parado")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isIncomingCall = intent?.getBooleanExtra("isIncomingCall", false)
        val phoneNumber = intent?.getStringExtra("phoneNumber") ?: ""

        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.START.toString() -> stopSelf()
        }
        return START_STICKY
    }

    private fun start() {
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "vale_voip_channel"
            val channel = NotificationChannel(
                CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)

            val notification = createNotification("VoIP Service", "Monitorando chamadas")
            startForeground(1, notification)
        }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "vale_voip_channel"
            val channel = NotificationChannel(
                CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

            val notification = createNotification("VoIP Service", "Monitorando chamadas")
            startForeground(1, notification)
        }
    }

    private fun createNotification(title: String, message: String): Notification {
        // Cria uma notificação para manter o service em foreground
        return NotificationCompat.Builder(this, "vale_voip_channel")
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun observeLinphoneEvents() {
        // Observa os eventos de chamadas e registros

        serviceScope.launch {
            linphoneManager.callStateFlow.collect { state ->
                if (state != null) {
                    handleCallState(state)
                }
            }
        }

        serviceScope.launch {
            linphoneManager.registrationStateFlow.collect { registrationState ->
                registrationState?.let {
                    handleRegistrationState(it)
                }
            }
        }

    }

    private fun handleCallState(state: Call.State) {
        when (state) {
            Call.State.IncomingReceived -> {
                // Chamada recebida
                logEvent("Recebendo chamada")
                showIncomingCallNotification()
//                launchCallScreen() // Lança a CallScreen mesmo com o app fechado
            }

            Call.State.Connected -> {
                // Chamada conectada, atualizar a interface de chamada
            }
            // Tratar outros estados conforme necessário
            else -> {

            }
        }
    }

    private fun handleRegistrationState(state: RegistrationState) {
        // Trata os estados de registro de conta (exemplo: reconexão, falha, etc.)
        logEvent("handleRegistrationState / state = ${state.name}")
    }

    private fun launchCallScreen() {
        logEvent("Recebendo chamada - launchCallScreen startactivity")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showIncomingCallNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE é necessário no Android 12+
        )

        val notification = NotificationCompat.Builder(this, "vale_voip_channel")
            .setContentTitle("Ligação em andamento")
            .setContentText("A chamada está ativa")
            .setSmallIcon(R.drawable.logo_vale_voip)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Remove a notificação ao tocar
            .setOngoing(true)
            .build()

        // Exibe a notificação
        val notificationId = 1001
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }


    /*private fun showIncomingCallNotification() {
        // Mostra uma notificação para chamadas recebidas
        val notification = createNotification("Nova Chamada", "Você tem uma nova chamada VoIP")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(1, notification)
    }*/

    private fun logEvent(string: String) {
        Log.d("ALE", "CallService | $string")
    }

    enum class Actions {
        START, STOP
    }
}