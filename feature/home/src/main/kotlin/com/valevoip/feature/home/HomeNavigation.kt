package com.valevoip.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.valevoip.core.navigation.route.HomeGraphRoute
import com.valevoip.core.navigation.route.HomeStartRoute
import com.valevoip.feature.home.screen.HomeScreen

/**
 * Sub-grafo da tela principal com as abas internas.
 * O AppNavHost declara este bloco e fornece os callbacks de saída.
 */
fun NavGraphBuilder.mainGraph(
    onNavigateToCall: (String) -> Unit,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    navigation<HomeGraphRoute>(startDestination = HomeStartRoute) {
        composable<HomeStartRoute> {
            HomeScreen(onNavigateToCall = onNavigateToCall, nestedGraph = nestedGraph)
        }
    }
}
