package com.valevoip.feature.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.route.LoginRoute

fun NavGraphBuilder.loginScreen(
    onNavigateToDialer: () -> Unit
) {
    composable<LoginRoute> {
        LoginScreen(
            onNavigateToDialer = onNavigateToDialer
        )
    }
}
