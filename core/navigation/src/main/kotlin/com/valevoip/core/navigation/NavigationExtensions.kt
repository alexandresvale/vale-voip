package com.valevoip.core.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navega para uma rota da BottomBar garantindo o comportamento correto de
 * salvar estado, evitar duplicidade e limpar a pilha.
 *
 * Esse é o padrão recomendado pelo Google para BottomNavigation:
 * - PopUp até o startDestination para evitar empilhamento infinito
 * - launchSingleTop para evitar múltiplas cópias
 * - restoreState para preservar scroll, inputs, etc.
 */
fun NavHostController.navigateToBottomBarRoute(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
