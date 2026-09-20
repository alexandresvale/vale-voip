package com.valevoip.core.navigation

import androidx.navigation.NavController

/**
 * Navega para a rota garantindo que não haja duplicatas na pilha.
 * Útil para navegações globais (ex: abrir tela de chamada).
 */
fun NavController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * popBackStack seguro — evita crash se a pilha já estiver vazia.
 * Retorna true se conseguiu voltar, false se já estava na raiz.
 */
fun NavController.safePopBackStack(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        false
    }
}
