package com.valevoip.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
//import com.valevoip.app.presentation.feature.dialer.DialerScreen
//import com.valevoip.app.presentation.feature.history.HistoryScreen
import com.valevoip.feature.home.model.BottomBarScreen

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Dialer.route
    ) {
        nestedGraph()
        composable(route = BottomBarScreen.Settings.route) {
            Box(Modifier.fillMaxSize()) { Text("Configurações") }
        }
    }
}
