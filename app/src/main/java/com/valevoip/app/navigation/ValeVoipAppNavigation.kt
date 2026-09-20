package com.valevoip.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.valevoip.feature.call.CALL_ROUTE
import com.valevoip.feature.call.callScreen
import com.valevoip.feature.dialer.dialerScreen
import com.valevoip.feature.history.historyScreen
import com.valevoip.feature.home.HOME_ROUTE
import com.valevoip.feature.home.homeScreen
import com.valevoip.feature.login.LOGIN_ROUTE
import com.valevoip.feature.login.loginScreen
import com.valevoip.feature.splash.SPLASH_ROUTE
import com.valevoip.feature.splash.splashScreen

/**
 * Este é o Roteador Global do Vale VoIP (O App Shell).
 * Ele orquestra os Módulos de Feature de forma totalmente independente e desacoplada.
 */
@Composable
fun ValeVoipAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE
    ) {

        // 1. Splash Screen
        splashScreen(
            onNavigateToMain = {
                navController.navigate(HOME_ROUTE) {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate("login_graph") {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                }
            }
        )

        // 2. Módulo de Login
        loginScreen(
            onNavigateToDialer = {
                navController.navigate(HOME_ROUTE) { popUpTo(LOGIN_ROUTE) { inclusive = true } }
            }
        )

        // 3. Módulo Main (Scaffold com as abas de Dialer e History)
        homeScreen(
            nestedGraph = {
                dialerScreen(
                    onNavigateToCall = { number ->
                        navController.navigate("call_route/$number")
                    }
                )

                // Extension que veio do módulo History
                historyScreen(
                    onNavigateToCall = { number ->
                        navController.navigate("call_route/$number")
                    }
                )
            }
        )

        // 4. Módulo de Chamada Ativa (Onde o Linphone age)
        callScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
