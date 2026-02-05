package com.valevoip.app.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.valevoip.app.presentation.feature.call.CallScreen
import com.valevoip.app.presentation.feature.debug.DebugScreen
import com.valevoip.app.presentation.feature.main.MainViewModel
import com.valevoip.app.presentation.feature.main.screen.MainScreen
import com.valevoip.app.presentation.feature.onboarding.OnboardingScreen
import com.valevoip.app.presentation.feature.splash.SplashScreen

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()

    // LÓGICA DE NAVEGAÇÃO AUTOMÁTICA ---
    LaunchedEffect(Unit) {
        mainViewModel.navigationChannel.collect { number ->
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute?.startsWith(Destinations.Call.route) == false) {
                Log.d("AppNavigation", "Navegando para atender: $number")
                navController.navigate("${Destinations.Call.route}/number=$number&isIncoming=true")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.SplashScreen.route,
    ) {
        // SPLASH
        composable(route = Destinations.SplashScreen.route) {
            SplashScreen(
                onNavigate = { hasConfig ->
                    val destination = if (hasConfig) Destinations.Main.route else Destinations.Onboarding.route
                    navController.navigate(destination) {
                        popUpTo(Destinations.SplashScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        // ONBOARDING
        composable(route = Destinations.Onboarding.route) {
            OnboardingScreen(
                onNavigateToDialer = {
                    navController.navigate(Destinations.Main.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Destinations.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        // MAIN SCREEN (Onde mora o Discador)
        composable(Destinations.Main.route) {
            MainScreen(
                mainViewModel = mainViewModel,
                onNavigateToCall = { number ->
                    navController.navigate("${Destinations.Call.route}/number=$number&isIncoming=false")
                }
            )
        }
        // CALL SCREEN (Tela Cheia / Full Screen)
        composable(
            route = "${Destinations.Call.route}/number={number}&isIncoming={isIncoming}",
            arguments = listOf(
                navArgument("number") { type = NavType.StringType },
                navArgument("isIncoming") { type = NavType.BoolType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
        ) { backStackEntry ->
            CallScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Debug de funcionalidade
        composable(Destinations.Debug.route) {
            DebugScreen()
        }
    }
}