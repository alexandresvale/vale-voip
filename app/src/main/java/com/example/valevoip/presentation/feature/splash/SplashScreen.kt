package com.example.valevoip.presentation.feature.splash

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.valevoip.presentation.feature.main.MainViewModel
import com.example.valevoip.presentation.ui.util.SystemBarsController

@Composable
fun SplashScreen(
    onNavigate: (hasConfig: Boolean) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.checkUseConfig()
    }

    uiState.hasUserConfig?.let {
        onNavigate(it)
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