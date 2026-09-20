package com.valevoip.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.valevoip.feature.call.callScreen
import com.valevoip.feature.call.navigateToCall
import com.valevoip.feature.dialer.dialerScreen
import com.valevoip.feature.history.historyScreen
import com.valevoip.feature.home.homeScreen
import com.valevoip.feature.home.navigateToHome
import com.valevoip.feature.login.LOGIN_ROUTE
import com.valevoip.feature.login.loginScreen
import com.valevoip.feature.login.navigateToLogin
import com.valevoip.feature.splash.SPLASH_ROUTE
import com.valevoip.feature.splash.splashScreen

/**
 * Este é o Roteador Global do Vale VoIP (O App Shell).
 * Ele orquestra os Módulos de Feature de forma totalmente independente e desacoplada.
 *
 * Cada feature expõe:
 * - NavGraphBuilder.featureScreen() → registra a tela no grafo
 * - NavController.navigateToFeature() → encapsula como navegar para ela
 *
 * O App Shell apenas conecta os eventos de saída de uma feature
 * às funções de navegação de entrada de outra.
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
                navController.navigateToHome(popUpFromRoute = SPLASH_ROUTE)
            },
            onNavigateToLogin = {
                navController.navigateToLogin(popUpFromRoute = SPLASH_ROUTE)
            }
        )

        // 2. Módulo de Login
        loginScreen(
            onNavigateToDialer = {
                navController.navigateToHome(popUpFromRoute = LOGIN_ROUTE)
            }
        )

        // 3. Módulo Home (Scaffold com as abas de Dialer e History)
        homeScreen(
            onNavigateToCall = { number ->
                navController.navigateToCall(number)
            },
            nestedGraph = {
                dialerScreen(
                    onNavigateToCall = { number ->
                        navController.navigateToCall(number)
                    }
                )

                historyScreen(
                    onNavigateToCall = { number ->
                        navController.navigateToCall(number)
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
