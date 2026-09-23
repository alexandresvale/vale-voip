package com.valevoip.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.valevoip.core.navigation.NAV_ANIMATION_DURATION_MS
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import com.valevoip.core.navigation.route.CallRoute
import com.valevoip.core.navigation.route.HomeGraphRoute
import com.valevoip.core.navigation.route.LoginRoute
import com.valevoip.core.navigation.route.SplashRoute
import com.valevoip.feature.call.callScreen
import com.valevoip.feature.dialer.dialerScreen
import com.valevoip.feature.history.historyScreen
import com.valevoip.feature.home.mainGraph
import com.valevoip.feature.login.loginScreen
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
fun AppNavHost(navigationCommandBus: NavigationCommandBus) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navigationCommandBus.commands.collect { command ->
            when (command) {
                is NavigationCommand.ToCall -> navController.navigate(CallRoute(number = command.number)) {
                    launchSingleTop = true
                }

                is NavigationCommand.ToHome -> navController.navigate(HomeGraphRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }

                is NavigationCommand.Back -> navController.popBackStack()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        }
    ) {
        // 1. Splash Screen
        splashScreen(
            onNavigateToMain = {
                navController.navigate(HomeGraphRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate(LoginRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            }
        )

        // 2. Módulo de Login
        loginScreen(
            onNavigateToDialer = {
                navController.navigate(HomeGraphRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            }
        )

        // 3. Módulo Home (Scaffold com as abas de Dialer e History)
        mainGraph(
            onNavigateToCall = { number ->
                navController.navigate(CallRoute(number)) {
                    launchSingleTop = true
                }
            },
            nestedGraph = {
                dialerScreen(
                    onNavigateToCall = { number ->
                        navController.navigate(CallRoute(number))
                    }
                )

                historyScreen(
                    onNavigateToCall = { number ->
                        navController.navigate(CallRoute(number))
                    }
                )
            }
        )

        // 4. Módulo de Chamada Ativa (Onde o Linphone age)
        callScreen(
            onNavigateBack = {
                val prevRoute = navController.previousBackStackEntry?.destination?.route
                if (prevRoute?.contains("SplashRoute") == true) {
                    // A tela foi aberta via Deep Link (Notificação).
                    // O Navigation coloca a Splash no fundo do BackStack sintético.
                    // Em vez de voltar pra Splash, pulamos direto para a Home!
                    navController.navigate(HomeGraphRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                } else {
                    // Fluxo normal do app
                    navController.popBackStack()
                }
            }
        )
    }
}
