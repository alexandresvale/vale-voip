package com.example.valevoip.main.presentation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.valevoip.core.extension.requestPermissions
import com.example.valevoip.core.lib.linphone.LinphoneManagerInternal
import com.example.valevoip.core.service.CallService
import com.example.valevoip.navigation.Destinations
import com.example.valevoip.navigation.SetupNavGraph
import com.example.valevoip.ui.theme.ValeVoipTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private var openAlertDialog = false

    private val useSipPermission = arrayOf(
        Manifest.permission.USE_SIP,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.USE_SIP,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var manager: LinphoneManagerInternal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        lifecycleScope.launch {
            delay(5000)
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

        setContent {
            ValeVoipTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = Destinations.Debug.route,
                    navController = navController
                )
            }
        }
//        mainViewModel.checkUseConfig()

    }

    private fun startCallService() {
        val intent = Intent(this, CallService::class.java)
        intent.action = CallService.Actions.START.toString()
        startService(intent)
    }


    private fun logEvent(string: String) {
        Log.d("ALE", string)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        private fun loadPjsua2() {
            try {
                System.loadLibrary("pjsua2")
                Log.d("MainActivity", "Library pjsua2 loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("MainActivity", "Failed to load library pjsua2", e)
            }
        }
    }
}