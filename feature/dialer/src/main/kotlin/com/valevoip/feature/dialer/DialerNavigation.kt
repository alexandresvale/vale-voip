package com.valevoip.feature.dialer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.route.DialerRoute

fun NavGraphBuilder.dialerScreen(onNavigateToCall: (String) -> Unit) {
    composable<DialerRoute> {
        DialerScreen(onNavigateToCall = onNavigateToCall)
    }
}
