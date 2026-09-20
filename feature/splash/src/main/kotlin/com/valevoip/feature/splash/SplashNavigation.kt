package com.valevoip.feature.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE = "splash_route"

fun NavGraphBuilder.splashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    composable(route = SPLASH_ROUTE) {
        SplashScreen(
            onNavigate = { hasConfig ->
                if (hasConfig) onNavigateToMain() else onNavigateToLogin()
            }
        )
    }
}
