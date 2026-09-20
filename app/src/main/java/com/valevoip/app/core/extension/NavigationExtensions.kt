package com.valevoip.app.core.extension

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navega para uma rota da BottomBar garantindo o comportamento correto de
 * salvar estado, evitar duplicidade e limpar a pilha.
 */
fun NavHostController.navigateToBottomBarRoute(route: String) {
    val navController = this
    navController.navigate(route) {
        // 1. PopUp até o início do grafo para evitar empilhamento infinito
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // 2. Evita múltiplas cópias da mesma tela na pilha
        launchSingleTop = true
        // 3. Restaura o estado anterior (scroll, inputs, etc.)
        restoreState = true
    }
}