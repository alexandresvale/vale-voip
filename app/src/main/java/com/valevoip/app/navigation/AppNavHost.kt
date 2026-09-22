package com.valevoip.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import com.valevoip.feature.call.callScreen
import com.valevoip.feature.dialer.dialerScreen
import com.valevoip.feature.history.historyScreen
import com.valevoip.feature.home.HOME_GRAPH_ROUTE
import com.valevoip.feature.home.mainGraph
import com.valevoip.feature.login.LOGIN_ROUTE

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
fun AppNavHost(navigationCommandBus: NavigationCommandBus) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navigationCommandBus.commands.collect { command ->
            when (command) {
                is NavigationCommand.ToCall -> navController.navigate("call_route/${command.number}")
                is NavigationCommand.ToHome -> navController.navigate(HOME_GRAPH_ROUTE) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }

                is NavigationCommand.Back -> navController.popBackStack()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AUTH_GRAPH_ROUTE
    ) {
        // Fluxo de autenticação: Splash → Login → Home
        authGraph(
            onAuthenticated = {
                navController.navigate(HOME_GRAPH_ROUTE) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )

        // Tela principal com abas (Dialer, History)

        // 1. Splash Screen
        /*splashScreen(
            onNavigateToMain = {
                navController.navigateToHome(popUpFromRoute = SPLASH_ROUTE)
            },
            onNavigateToLogin = {
                navController.navigateToLogin(popUpFromRoute = SPLASH_ROUTE)
            }
        )*/

        // 2. Módulo de Login
        /*loginScreen(
            onNavigateToDialer = {
                navController.navigateToHome(popUpFromRoute = LOGIN_ROUTE)
            }
        )*/

        // 3. Módulo Home (Scaffold com as abas de Dialer e History)
        mainGraph(
            onNavigateToCall = { number ->
                navController.navigate("call_route/$number")
            },
            nestedGraph = {
                dialerScreen(
                    onNavigateToCall = { number ->
                        navController.navigate("call_route/$number")
                    }
                )

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
