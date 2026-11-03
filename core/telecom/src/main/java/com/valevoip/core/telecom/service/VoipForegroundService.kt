package com.valevoip.core.telecom.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetAccountUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ObserveRegistrationStateUseCase
import com.valevoip.core.domain.usecase.ObserveSpeakerStateUseCase
import com.valevoip.core.domain.usecase.ObserveVoipSettingsUseCase
import com.valevoip.core.domain.usecase.RegisterAccountUseCase
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    lateinit var observeRegistrationStateUseCase: ObserveRegistrationStateUseCase

    @Inject
    lateinit var observeSpeakerStateUseCase: ObserveSpeakerStateUseCase

    @Inject
    lateinit var registerAccountUseCase: RegisterAccountUseCase

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

    @Inject
    lateinit var getAccountUseCase: GetAccountUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var callObserverJob: Job? = null
    private var proximityObserverJob: Job? = null
    private var registrationObserverJob: Job? = null
    private var settingsObserverJob: Job? = null

    private var isCallActive: Boolean = false
    private var vibrateOnIncomingCall: Boolean = true
    private var currentUsername: String? = null
    private var currentRegistrationState: SipRegistrationState = SipRegistrationState.None

    companion object {
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, VoipForegroundService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoipForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("VoipForegroundService criado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand action=${intent?.action}")

        when (intent?.action) {
            ACTION_START_MONITORING, null -> startMonitoring()
            VoipNotificationManager.ACTION_HANGUP -> performHangup()
            VoipNotificationManager.ACTION_ANSWER -> performAnswer()
            ACTION_STOP_SERVICE -> stopMonitoring()
        }

        return START_STICKY
    }

    private fun stopMonitoring() {
        Timber.d("stopMonitoring: encerrando foreground e parando serviço.")
        vibratorManager.stopVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancelAll()
        wakeLockManager.release()
        stopSelf()
    }

    private fun startMonitoring() {
        currentRegistrationState = observeRegistrationStateUseCase().value
        // Garante promoção imediata em menos de 5s para evitar ForegroundServiceDidNotStartInTimeException em cold boot
        promoteServiceForeground(
            inCall = isCallActive,
            needsMicrophone = false
        )

        ensureObserversAreRunning()

        serviceScope.launch {
            val account = getAccountUseCase()
            if (account == null) {
                Timber.w("startMonitoring: nenhuma conta SIP salva. Encerrando serviço.")
                stopMonitoring()
                return@launch
            }

            currentUsername = account.username
            notificationManager.updateServiceStatusNotification(
                username = currentUsername,
                state = observeRegistrationStateUseCase().value
            )

            // Idempotente em LinphoneSipClient: só renegocia se ainda não estiver conectado/conectando
            registerAccountUseCase(
                username = account.username,
                password = account.password,
                domain = account.domain,
                transport = account.transport
            )
        }
    }

    private fun ensureObserversAreRunning() {
        if (settingsObserverJob?.isActive != true) {
            settingsObserverJob = observeVoipSettingsUseCase()
                .onEach { settings ->
                    vibrateOnIncomingCall = settings.vibrateOnIncomingCall
                    Timber.d("Configuração de vibração atualizada: $vibrateOnIncomingCall")
                }
                .launchIn(serviceScope)
        }

        if (registrationObserverJob?.isActive != true) {
            registrationObserverJob = observeRegistrationStateUseCase()
                .onEach { state ->
                    currentRegistrationState = state
                    notificationManager.updateServiceStatusNotification(
                        username = currentUsername,
                        state = state
                    )
                }
                .launchIn(serviceScope)
        }

        if (proximityObserverJob?.isActive != true) {
            proximityObserverJob = combine(
                observeCallStateUseCase(),
                observeSpeakerStateUseCase()
            ) { status, isSpeakerEnabled ->
                when (status) {
                    CallStatus.DIALING,
                    CallStatus.RINGING,
                    CallStatus.ACTIVE -> !isSpeakerEnabled

                    CallStatus.IDLE,
                    CallStatus.INCOMING,
                    CallStatus.ENDED -> false
                }
            }
                .distinctUntilChanged()
                .onEach { enableProximity ->
                    wakeLockManager.setProximitySensorEnabled(enableProximity)
                }
                .launchIn(serviceScope)
        }

        if (callObserverJob?.isActive != true) {
            Timber.d("Iniciando observer de estado de chamada")
            callObserverJob = observeCallStateUseCase()
                .distinctUntilChanged()
                .onEach { status -> handleCallStatusChange(status) }
                .launchIn(serviceScope)
        }
    }

    private fun handleCallStatusChange(status: CallStatus) {
        Timber.d("Status de chamada recebido: $status")

        when (status) {
            CallStatus.ENDED,
            CallStatus.IDLE -> {
                isCallActive = false
                vibratorManager.stopVibration()
                notificationManager.dismissCallNotification()
                wakeLockManager.release()
                promoteServiceForeground(inCall = false, needsMicrophone = false)
            }

            CallStatus.INCOMING -> {
                isCallActive = true
                promoteServiceForeground(inCall = true, needsMicrophone = false)
                if (vibrateOnIncomingCall) {
                    vibratorManager.startRingingVibration()
                }
                val number = getCurrentCallNumberUseCase()
                showCallNotification(number = number, isIncoming = true)

                // Dispara comando de navegação: abre imediato em foreground ou aguarda em buffer até abertura pela notificação
                navigationCommandBus.navigate(NavigationCommand.ToCall(number))
            }

            CallStatus.DIALING,
            CallStatus.RINGING -> {
                isCallActive = true
                vibratorManager.stopVibration()
                promoteServiceForeground(inCall = true, needsMicrophone = true)
                val number = getCurrentCallNumberUseCase()
                showCallNotification(number = number, isIncoming = false)
            }

            CallStatus.ACTIVE -> {
                isCallActive = true
                vibratorManager.stopVibration()
                promoteServiceForeground(inCall = true, needsMicrophone = true)
                val number = getCurrentCallNumberUseCase()
                showCallNotification(number = number, isIncoming = false)
            }
        }
    }

    private fun promoteServiceForeground(inCall: Boolean, needsMicrophone: Boolean) {
        val notification = notificationManager.buildServiceStatusNotification(
            username = currentUsername,
            state = currentRegistrationState
        )
        val desiredType = resolveForegroundServiceType(inCall = inCall, needsMicrophone = needsMicrophone)

        try {
            ServiceCompat.startForeground(
                this,
                VoipNotificationManager.SERVICE_NOTIFICATION_ID,
                notification,
                desiredType
            )
        } catch (securityException: SecurityException) {
            Timber.w(
                securityException,
                "Promoção FGS com microfone recusada em background; aplicando fallback sem MICROPHONE."
            )
            val fallbackType = resolveForegroundServiceType(inCall = inCall, needsMicrophone = false)
            ServiceCompat.startForeground(
                this,
                VoipNotificationManager.SERVICE_NOTIFICATION_ID,
                notification,
                fallbackType
            )
        }
    }

    private fun resolveForegroundServiceType(inCall: Boolean, needsMicrophone: Boolean): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                if (inCall) {
                    type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    if (needsMicrophone && hasRecordAudioPermission()) {
                        type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    }
                }
                type
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (inCall) {
                    var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    if (needsMicrophone && hasRecordAudioPermission()) {
                        type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    }
                    type
                } else {
                    0
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (inCall) ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL else 0
            }

            else -> 0
        }
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showCallNotification(number: String, isIncoming: Boolean) {
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
            promoteServiceForeground(inCall = true, needsMicrophone = true)
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
        Timber.d("VoipForegroundService destruído")
    }
}
