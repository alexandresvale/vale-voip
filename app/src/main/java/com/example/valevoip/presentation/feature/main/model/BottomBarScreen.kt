package com.example.valevoip.presentation.feature.main.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    object Dialer : BottomBarScreen(
        route = "dialer",
        title = "Discador",
        icon = Icons.Outlined.Call,
        iconSelected = Icons.Filled.Call
    )

    object History : BottomBarScreen(
        route = "history",
        title = "Histórico",
        icon = Icons.Outlined.DateRange,
        iconSelected = Icons.Filled.DateRange
    )

    object Settings : BottomBarScreen(
        route = "settings",
        title = "Configuração",
        icon = Icons.Outlined.Settings,
        iconSelected = Icons.Filled.Settings
    )
}