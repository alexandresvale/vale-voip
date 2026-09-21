package com.valevoip.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.valevoip.feature.home.screen.HomeScreen

const val HOME_ROUTE = "home_graph"

/**
 * Navega para a tela Home limpando a pilha até a rota de origem (inclusive).
 * Garante que o usuário não consiga voltar para Splash ou Login.
 */
fun NavController.navigateToHome(popUpFromRoute: String) {
    navigate(HOME_ROUTE) {
        popUpTo(popUpFromRoute) { inclusive = true }
    }
}

fun NavGraphBuilder.homeScreen(
    onNavigateToCall: (String) -> Unit,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    composable(
        route = HOME_ROUTE,
        deepLinks = listOf(navDeepLink { uriPattern = "valevoip://home" })
    ) {
        HomeScreen(
            onNavigateToCall = onNavigateToCall,
            nestedGraph = nestedGraph
        )
    }
}