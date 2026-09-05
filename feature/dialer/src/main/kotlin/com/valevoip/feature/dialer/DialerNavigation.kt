package com.valevoip.feature.dialer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val DIALER_ROUTE = "dialer_route"

fun NavGraphBuilder.dialerScreen(onNavigateToCall: (String) -> Unit) {
    composable(route = DIALER_ROUTE) {
        DialerScreen(onNavigateToCall = onNavigateToCall)
    }
}
