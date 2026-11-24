package com.valevoip.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.valevoip.app.navigation.AppNavHost
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationCommandBus: NavigationCommandBus

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValeVoipTheme {
                Timber.d("MainActivity iniciou")
                AppNavHost(navigationCommandBus = navigationCommandBus)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationDeepLink(intent)
    }

    private fun handleNotificationDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "valevoip" && data.host == "call") {
            val number = data.getQueryParameter("number").orEmpty()
            navigationCommandBus.navigate(NavigationCommand.ToCall(number))
        }
    }
}
