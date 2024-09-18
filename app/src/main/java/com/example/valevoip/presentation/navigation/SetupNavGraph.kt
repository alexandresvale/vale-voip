package com.example.valevoip.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.valevoip.presentation.feature.onboarding.presentation.screen.OnboardingScreen
import com.example.valevoip.presentation.main.presentation.screen.MainScreen
import com.example.valevoip.presentation.feature.debug.DebugContent
import com.example.valevoip.presentation.feature.debug.DebugScreen

@Composable
fun SetupNavGraph(
    startDestination: String = Destinations.Onboarding.route,
    navController: NavHostController,
) {
    NavHost(
        startDestination = startDestination,
        navController = navController,
    ) {
        onboardingRoute()
        composable(Destinations.Main.route) {
            MainScreen()
        }
        composable(Destinations.Dialer.route) {
//            DialerScreen()
        }
        composable(Destinations.History.route) {
            //HistoryScreen()
        }
        composable(Destinations.Debug.route) {
            DebugScreen()
        }
    }
}


private fun NavGraphBuilder.onboardingRoute() {
    composable(route = Destinations.Onboarding.route) {
        OnboardingScreen()
    }
}