package com.valevoip.app.core.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.valevoip.app.VALEVOIP_TAG
import com.valevoip.app.core.notification.CallNotificationManager
import com.valevoip.app.core.notification.CallNotificationManager.Companion.CALL_NOTIFICATION_ID
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
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
class CallService : Service() {

    @Inject
    lateinit var notificationManager: CallNotificationManager

    @Inject
    lateinit var hangUpUseCase: com.valevoip.core.domain.usecase.HangUpUseCase

    @Inject
    lateinit var answerCallUseCase: com.valevoip.core.domain.usecase.AnswerCallUseCase

    @Inject
    lateinit var observeCallStateUseCase: com.valevoip.core.domain.usecase.ObserveCallStateUseCase

    @Inject
    lateinit var getCurrentCallNumberUseCase: com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase

    //    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    @Inject
    lateinit var hardwareManager: CallHardwareManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null
    private var isCallActive: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logEvent("Service onStartCommand intent = $intent")
        when (intent?.action) {
            ACTIONS.START_MONITORING -> startMonitoring()
            CallNotificationManager.ACTION_HANGUP -> performHangup()
            CallNotificationManager.ACTION_ANSWER -> performAnswer()
            ACTIONS.STOP_SERVICE -> stopSelf()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isCallActive) {
            logEvent("Service START_MONITORING ignorado: Chamada em andamento. Mantendo notificação atual.")
            ensureObserverIsRunning()
            return
        }
        logEvent("Service: Iniciando/Atualizando Notificação Persistente (Online)")
        val onlineNotification = notificationManager.buildOnlineNotification()
        startForeground(CallNotificationManager.SERVICE_NOTIFICATION_ID, onlineNotification)
        ensureObserverIsRunning()
    }

    private fun ensureObserverIsRunning() {
        if (observerJob?.isActive == true) {
            logEvent("Observer já está rodando. Ignorando criação")
            return
        }
        logEvent("Iniciando NOVO Job de Observer")
        observerJob = observeCallStateUseCase()
            .onEach { status ->
                logEvent("Status recebido: $status")
                when (status) {
                    _root_ide_package_.com.valevoip.core.domain.model.CallStatus.ENDED, _root_ide_package_.com.valevoip.core.domain.model.CallStatus.IDLE -> {
                        isCallActive = false
                        val manager = getSystemService(NotificationManager::class.java)
                        manager.cancel(CALL_NOTIFICATION_ID)
                        hardwareManager.releaseSensors()
                    }

                    _root_ide_package_.com.valevoip.core.domain.model.CallStatus.INCOMING, _root_ide_package_.com.valevoip.core.domain.model.CallStatus.DIALING, _root_ide_package_.com.valevoip.core.domain.model.CallStatus.RINGING -> {
                        updateCallNotification(isIncoming = (status == _root_ide_package_.com.valevoip.core.domain.model.CallStatus.INCOMING))
                        hardwareManager.activateSensors()
                    }

                    _root_ide_package_.com.valevoip.core.domain.model.CallStatus.ACTIVE -> {
                        updateCallNotification(isIncoming = false)
                        hardwareManager.activateSensors()
                    }
                }
            }
            .launchIn(serviceScope)
    }

    private fun updateCallNotification(isIncoming: Boolean) {
        isCallActive = true
        val number = getCurrentCallNumberUseCase()
        val notification = notificationManager.buildForegroundNotification(number, isIncoming)
        logEvent("Exibindo Notificação de Chamada (ID: $CALL_NOTIFICATION_ID) para $number")
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun performHangup() {
        serviceScope.launch {
            hangUpUseCase()
            logEvent("Service performHangup")
        }
    }

    private fun performAnswer() {
        serviceScope.launch {
            answerCallUseCase()
            logEvent("Service performAnswer")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        serviceScope.cancel()
        notificationManager.cancelAll()
        hardwareManager.releaseSensors()
        logEvent("Service parado")
    }

    override fun onCreate() {
        super.onCreate()
        logEvent("Service iniciado")
    }

    private fun logEvent(string: String) {
        Log.d(VALEVOIP_TAG, "CallService | $string")
    }

    object ACTIONS {
        const val START_MONITORING = "START_MONITORING"
        const val STOP_SERVICE = "STOP_SERVICE"
    }
}