package com.valevoip.feature.splash

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valevoip.core.designsystem.util.SystemBarsController

//import com.valevoip.app.presentation.feature.main.MainViewModel

@Composable
internal fun SplashScreen(
    onNavigate: (hasConfig: Boolean) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    SystemBarsController(useDarkIcons = false)

    LaunchedEffect(isUserLoggedIn) {
        Log.d("ValeVoIP", "SplashScreen LaunchedEffect $isUserLoggedIn")
        isUserLoggedIn?.let { loggedIn ->
            onNavigate(loggedIn)
        }
    }

    SplashLayout()
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        onNavigate = { }
    )
}