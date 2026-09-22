package com.valevoip.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.valevoip.feature.login.loginScreen
import com.valevoip.feature.splash.SPLASH_ROUTE
import com.valevoip.feature.splash.splashScreen

const val AUTH_GRAPH_ROUTE = "auth_graph"

/**
 * Sub-grafo de autenticação: Splash → Login.
 * Encapsula todo o fluxo de entrada no app.
 * onAuthenticated é chamado quando o usuário está pronto para ir à Home.
 */
fun NavGraphBuilder.authGraph(
    onAuthenticated: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    navigation(
        startDestination = SPLASH_ROUTE,
        route = AUTH_GRAPH_ROUTE
    ) {
        splashScreen(
            onNavigateToMain = onAuthenticated,
            onNavigateToLogin = onNavigateToLogin
        )
        loginScreen(
            onNavigateToDialer = onAuthenticated
        )
    }
}