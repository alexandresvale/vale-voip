package com.valevoip.feature.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LOGIN_ROUTE = "login_graph"

fun NavGraphBuilder.loginScreen(
    onNavigateToDialer: () -> Unit
) {
    composable(route = LOGIN_ROUTE) {
        LoginScreen(
            onNavigateToDialer = onNavigateToDialer
        )
    }
}
