package com.valevoip.feature.call

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.valevoip.core.navigation.NAV_ANIMATION_DURATION_MS
import com.valevoip.core.navigation.route.CallRoute

fun NavGraphBuilder.callScreen(onNavigateBack: () -> Unit) {
    composable<CallRoute>(
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(NAV_ANIMATION_DURATION_MS)
            )
        }
    ) { backStackEntry ->
        val callRoute = backStackEntry.toRoute<CallRoute>()
        CallScreen(
            //number = callRoute.number,
            onNavigateBack = onNavigateBack
        )
    }
}
