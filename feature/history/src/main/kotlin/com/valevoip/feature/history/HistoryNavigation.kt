package com.valevoip.feature.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val HISTORY_ROUTE = "history_route"

fun NavGraphBuilder.historyScreen(onNavigateToCall: (String) -> Unit) {
    composable(route = HISTORY_ROUTE) {
        HistoryScreen(onNavigateToCall = onNavigateToCall)
    }
}