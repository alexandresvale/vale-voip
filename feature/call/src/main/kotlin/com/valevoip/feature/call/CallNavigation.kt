package com.valevoip.feature.call

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink

const val CALL_ROUTE = "call_route/{number}"

/**
 * Navega para a tela de chamada ativa com o número fornecido.
 * Encapsula a construção da rota com o argumento.
 */
fun NavController.navigateToCall(number: String) {
    navigate("call_route/$number")
}

fun NavGraphBuilder.callScreen(onNavigateBack: () -> Unit) {
    composable(
        route = CALL_ROUTE,
        deepLinks = listOf(navDeepLink { uriPattern = "valevoip://call/{number}" })
    ) { backStackEntry ->
        val number = backStackEntry.arguments?.getString("number") ?: ""
        CallScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
