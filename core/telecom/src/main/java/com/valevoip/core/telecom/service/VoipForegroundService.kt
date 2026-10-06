package com.valevoip.core.telecom.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import com.valevoip.core.telecom.notification.VoipNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoipForegroundService : Service() {

    @Inject
    lateinit var notificationManager: VoipNotificationManager

    @Inject
    lateinit var hangUpUseCase: HangUpUseCase

    @Inject
    lateinit var answerCallUseCase: AnswerCallUseCase

    @Inject
    lateinit var observeCallStateUseCase: ObserveCallStateUseCase

    @Inject
    lateinit var navigationCommandBus: NavigationCommandBus

    @Inject
    lateinit var getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase

    @Inject
    lateinit var wakeLockManager: ProximityWakeLockManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null
    private var isCallActive: Boolean = false

    companion object {
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val TAG = "VALEVOIP_TELECOM"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        logEvent("Service criado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logEvent("onStartCommand action=${intent?.action}")

        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            VoipNotificationManager.ACTION_HANGUP -> performHangup()
            VoipNotificationManager.ACTION_ANSWER -> performAnswer()
            ACTION_STOP_SERVICE -> stopSelf()
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        if (isCallActive) {
            logEvent("START_MONITORING ignorado: chamada em andamento")
            ensureObserverIsRunning()
            return
        }

        logEvent("Iniciando notificação persistente (Online)")
        val onlineNotification = notificationManager.buildOnlineNotification()
        startForeground(VoipNotificationManager.SERVICE_NOTIFICATION_ID, onlineNotification)
        ensureObserverIsRunning()
    }

    private fun ensureObserverIsRunning() {
        if (observerJob?.isActive == true) {
            logEvent("Observer já está rodando")
            return
        }

        logEvent("Iniciando novo observer de estado de chamada")
        observerJob = observeCallStateUseCase()
            .onEach { status -> handleCallStatusChange(status) }
            .launchIn(serviceScope)
    }

    private fun handleCallStatusChange(status: CallStatus) {
        logEvent("Status recebido: $status")

        when (status) {
            CallStatus.ENDED, CallStatus.IDLE -> {
                isCallActive = false
                notificationManager.dismissCallNotification()
                wakeLockManager.release()
            }

            CallStatus.INCOMING -> {
                isCallActive = true
                val number = getCurrentCallNumberUseCase() ?: ""
                showCallNotification(isIncoming = true)
                wakeLockManager.acquire()

                // Dispara comando de navegação!
                // Se o app estiver aberto (foreground), a tela abre na hora.
                // Se estiver fechado (background), o comando fica no buffer e executa
                // no exato momento que o usuário clica na notificação e o app vem pro topo.
                navigationCommandBus.navigate(NavigationCommand.ToCall(number))
            }

            CallStatus.DIALING, CallStatus.RINGING -> {
                isCallActive = true
                showCallNotification(isIncoming = false)
                wakeLockManager.acquire()
            }

            CallStatus.ACTIVE -> {
                isCallActive = true
                showCallNotification(isIncoming = false)
                wakeLockManager.acquire()
            }
        }
    }

    private fun showCallNotification(isIncoming: Boolean) {
        val number = getCurrentCallNumberUseCase()
        logEvent("Exibindo notificação de chamada para $number (incoming=$isIncoming)")
        notificationManager.showCallNotification(
            contactName = number,
            isIncoming = isIncoming,
            serviceClass = VoipForegroundService::class.java
        )
    }

    private fun performHangup() {
        serviceScope.launch {
            logEvent("performHangup")
            hangUpUseCase()
        }
    }

    private fun performAnswer() {
        serviceScope.launch {
            logEvent("performAnswer")
            answerCallUseCase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        notificationManager.cancelAll()
        wakeLockManager.release()
        logEvent("Service destruído")
    }

    private fun logEvent(message: String) {
        Log.d(TAG, "VoipForegroundService | $message")
    }
}
