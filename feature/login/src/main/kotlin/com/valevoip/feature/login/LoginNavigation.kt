package com.valevoip.feature.login

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LOGIN_ROUTE = "login_graph"

/**
 * Navega para a tela de Login limpando a pilha até a rota de origem (inclusive).
 * Garante que o usuário não consiga voltar para a Splash.
 */
fun NavController.navigateToLogin(popUpFromRoute: String) {
    navigate(LOGIN_ROUTE) {
        popUpTo(popUpFromRoute) { inclusive = true }
    }
}

fun NavGraphBuilder.loginScreen(
    onNavigateToDialer: () -> Unit
) {
    composable(route = LOGIN_ROUTE) {
        LoginScreen(
            onNavigateToDialer = onNavigateToDialer
        )
    }
}
