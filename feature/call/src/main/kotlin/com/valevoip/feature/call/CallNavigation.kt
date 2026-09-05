package com.valevoip.feature.call

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val CALL_ROUTE = "call_route/{number}"
fun NavGraphBuilder.callScreen(onNavigateBack: () -> Unit) {
    composable(route = CALL_ROUTE) { backStackEntry ->
        val number = backStackEntry.arguments?.getString("number") ?: ""
        CallScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
