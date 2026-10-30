package com.valevoip.core.telecom.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ObserveVoipSettingsUseCase
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import com.valevoip.core.telecom.notification.VoipNotificationManager
import com.valevoip.core.telecom.vibration.VoipVibratorManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
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

    @Inject
    lateinit var vibratorManager: VoipVibratorManager

    @Inject
    lateinit var observeVoipSettingsUseCase: ObserveVoipSettingsUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null
    private var settingsObserverJob: Job? = null
    private var isCallActive: Boolean = false
    private var vibrateOnIncomingCall: Boolean = true

    companion object {
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val TAG = "VALEVOIP_TELECOM"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service criado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand action=${intent?.action}")

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
            Timber.d("START_MONITORING ignorado: chamada em andamento")
            ensureObserverIsRunning()
            return
        }

        Timber.d("Iniciando notificação persistente (Online)")
        val onlineNotification = notificationManager.buildOnlineNotification()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            VoipNotificationManager.SERVICE_NOTIFICATION_ID,
            onlineNotification,
            serviceType
        )

        ensureObserverIsRunning()
    }

    private fun ensureObserverIsRunning() {
        if (settingsObserverJob?.isActive != true) {
            settingsObserverJob = observeVoipSettingsUseCase()
                .onEach { settings ->
                    vibrateOnIncomingCall = settings.vibrateOnIncomingCall
                    Timber.d("Configuração de vibração atualizada: $vibrateOnIncomingCall")
                }
                .launchIn(serviceScope)
        }

        if (observerJob?.isActive == true) {
            Timber.d("Observer já está rodando")
            return
        }

        Timber.d("Iniciando novo observer de estado de chamada")
        observerJob = observeCallStateUseCase()
            .onEach { status -> handleCallStatusChange(status) }
            .launchIn(serviceScope)
    }

    private fun handleCallStatusChange(status: CallStatus) {
        Timber.d("Status recebido: $status")

        when (status) {
            CallStatus.ENDED, CallStatus.IDLE -> {
                isCallActive = false
                vibratorManager.stopVibration()
                notificationManager.dismissCallNotification()
                wakeLockManager.release()
            }

            CallStatus.INCOMING -> {
                isCallActive = true
                if (vibrateOnIncomingCall) {
                    vibratorManager.startRingingVibration()
                }
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
                vibratorManager.stopVibration()
                showCallNotification(isIncoming = false)
                wakeLockManager.acquire()
            }

            CallStatus.ACTIVE -> {
                isCallActive = true
                vibratorManager.stopVibration()
                showCallNotification(isIncoming = false)
                wakeLockManager.acquire()
            }
        }
    }

    private fun showCallNotification(isIncoming: Boolean) {
        val number = getCurrentCallNumberUseCase()
        Timber.d("Exibindo notificação de chamada para $number (incoming=$isIncoming)")
        notificationManager.showCallNotification(
            contactName = number,
            isIncoming = isIncoming,
            serviceClass = VoipForegroundService::class.java
        )
    }

    private fun performHangup() {
        serviceScope.launch {
            Timber.d("performHangup")
            hangUpUseCase()
        }
    }

    private fun performAnswer() {
        serviceScope.launch {
            Timber.d("performAnswer")
            answerCallUseCase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vibratorManager.stopVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        notificationManager.cancelAll()
        wakeLockManager.release()
        Timber.d("Service destruído")
    }
}
