package com.example.valevoip.presentation.feature.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.valevoip.core.extension.requestPermissions
import com.example.valevoip.core.service.CallService
import com.example.valevoip.navigation.AppNavigation
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                startCallService()
            },
            onDenied = {
                logEvent("Permissão não ok.")
            }
        )

        setupScreenWake()

        setContent {
            ValeVoipTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun setupScreenWake() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@MainActivity, null)
            }
        }
    }

    private fun startCallService() {
        val intent = Intent(this, CallService::class.java)
        intent.action = CallService.Actions.START.toString()
        startService(intent)
    }

    private fun logEvent(string: String) {
        Log.d("ALE", string)
    }

}