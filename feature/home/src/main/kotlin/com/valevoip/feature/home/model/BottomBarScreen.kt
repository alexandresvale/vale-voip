package com.valevoip.feature.home.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

import com.valevoip.core.navigation.route.DialerRoute
import com.valevoip.core.navigation.route.HistoryRoute
import com.valevoip.core.navigation.route.SettingsRoute

sealed class BottomBarScreen<T : Any>(
    val route: T,
    val title: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    data object Dialer : BottomBarScreen<DialerRoute>(
        route = DialerRoute,
        title = "Discador",
        icon = Icons.Outlined.Call,
        iconSelected = Icons.Filled.Call
    )

    data object History : BottomBarScreen<HistoryRoute>(
        route = HistoryRoute,
        title = "Histórico",
        icon = Icons.Outlined.DateRange,
        iconSelected = Icons.Filled.DateRange
    )

    data object Settings : BottomBarScreen<SettingsRoute>(
        route = SettingsRoute,
        title = "Configuração",
        icon = Icons.Outlined.Settings,
        iconSelected = Icons.Filled.Settings
    )
}
