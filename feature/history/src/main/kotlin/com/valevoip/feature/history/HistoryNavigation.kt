package com.valevoip.feature.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.route.HistoryRoute

const val HISTORY_ROUTE = "history_route"

fun NavGraphBuilder.historyScreen(onNavigateToCall: (String) -> Unit) {
    composable<HistoryRoute> {
        HistoryScreen(onNavigateToCall = onNavigateToCall)
    }
}