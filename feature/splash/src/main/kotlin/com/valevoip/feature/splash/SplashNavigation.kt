package com.valevoip.feature.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.route.SplashRoute

fun NavGraphBuilder.splashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    composable<SplashRoute> {
        SplashScreen(
            onNavigate = { hasConfig ->
                if (hasConfig) onNavigateToMain() else onNavigateToLogin()
            }
        )
    }
}
