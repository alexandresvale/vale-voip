package com.valevoip.feature.home

/*import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.valevoip.app.navigation.AppNavigation
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import dagger.hilt.android.AndroidEntryPoint*/

/*
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val HomeViewModel: HomeViewModel by viewModels()
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            logEvent("Todas as permissões essenciais concedidas.")
            checkFullScreenIntentPermission()
        } else {
            logEvent("Algumas permissões foram negadas. O app pode não funcionar corretamente.")
        }

    }

    private val useSipPermission = arrayOf(
        Manifest.permission.USE_SIP,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.USE_SIP,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE_CAMERA,
        Manifest.permission.CAMERA,
        Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            )
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = 500L
            slideUp.doOnEnd {
                splashScreenView.remove()
            }
            slideUp.start()
        }

        requestPermissions(
            permissions = useSipPermission,
            rationale = "É necessário conceder permissões para utilizar o SIP.",
            onGranted = {
                logEvent("Permissão ok.")
//                startCallService()
            },
            onDenied = {
                logEvent("Permissão não ok.")
            }
        )

        setupScreenWake()
        checkIntentForNavigation(intent)
        checkAndRequestPermissions()

        setContent {
            ValeVoipTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(HomeViewModel = HomeViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntentForNavigation(intent)
    }

    */
/**
     * Função para acorda a tela do celular.
     *//*

    private fun setupScreenWake() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Dispensa o Keyguard (Cadeado) se não tiver senha, para atender direto
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    private fun checkIntentForNavigation(intent: Intent?) {
        val shouldNavigate = intent?.getBooleanExtra("NAVIGATE_TO_CALL", false) == true
        val number = intent?.getStringExtra("CONTACT_NUMBER")

        if (shouldNavigate && number != null) {
            HomeViewModel.handleNotificationClick(number)
            intent.removeExtra("NAVIGATE_TO_CALL")
        }
    }

    private fun checkFullScreenIntentPermission() {
        // Essa verificação só existe no Android 14+ (API 34)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            if (!notificationManager.canUseFullScreenIntent()) {
                // A permissão não foi concedida! Precisamos pedir para o usuário ativar manualmente.
                AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("Para receber chamadas na tela de bloqueio, o ValeVoip precisa de permissão de tela cheia.")
                    .setPositiveButton("Ativar") { _, _ ->
                        // Abre a tela de configurações específica do App
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        // Se quiser ser específico para seu pacote (funciona em alguns devices):
                        intent.data = Uri.parse("package:$packageName")
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback para as configurações gerais do app
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            })
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    */
/*private fun startCallService() {
        val intent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTIONS.START_MONITORING
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }*//*


    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE, // Para pausar se chegar ligação GSM
            Manifest.permission.CAMERA // Só se tiver vídeo
        )

        // Android 13+ (Notificações)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Android 12+ (Bluetooth para fones sem fio)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        requestMultiplePermissions.launch(permissionsToRequest.toTypedArray())
    }

    private fun logEvent(string: String) {
        Log.d("ALE", string)
    }

}*/
