package com.valevoip.feature.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.NAV_ANIMATION_DURATION_MS
import com.valevoip.core.navigation.route.DialerRoute
import com.valevoip.core.navigation.route.HistoryRoute
import com.valevoip.core.navigation.route.SettingsRoute
import com.valevoip.feature.home.model.BottomBarScreen

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Dialer.route,
        enterTransition = {
            val initialIndex = getTabIndex(initialState.destination)
            val targetIndex = getTabIndex(targetState.destination)
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideIntoContainer(towards = direction, animationSpec = tween(NAV_ANIMATION_DURATION_MS))
        },
        exitTransition = {
            val initialIndex = getTabIndex(initialState.destination)
            val targetIndex = getTabIndex(targetState.destination)
            val direction = if (targetIndex >= initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            slideOutOfContainer(towards = direction, animationSpec = tween(NAV_ANIMATION_DURATION_MS))
        }
    ) {
        nestedGraph()
        composable<SettingsRoute> {
            Box(Modifier.fillMaxSize()) { Text("Configurações") }
        }
    }
}

private fun getTabIndex(destination: NavDestination?): Int {
    return when {
        destination?.hasRoute<DialerRoute>() == true -> 0
        destination?.hasRoute<HistoryRoute>() == true -> 1
        destination?.hasRoute<SettingsRoute>() == true -> 2
        else -> 0
    }
}
