package com.valevoip.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.feature.home.screen.HomeScreen

const val HOME_ROUTE = "home_graph"

fun NavGraphBuilder.homeScreen(
    onNavigateToCall: (String) -> Unit,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    composable(route = HOME_ROUTE) {
        HomeScreen(
            onNavigateToCall = onNavigateToCall,
            nestedGraph = nestedGraph
        )
    }
}