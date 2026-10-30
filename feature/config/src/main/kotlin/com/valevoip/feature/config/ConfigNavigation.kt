package com.valevoip.feature.config

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.valevoip.core.navigation.route.SettingsRoute

fun NavGraphBuilder.configScreen() {
    composable<SettingsRoute> {
        ConfigScreen()
    }
}
