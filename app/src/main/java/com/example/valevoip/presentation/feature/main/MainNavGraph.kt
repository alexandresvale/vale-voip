package com.example.valevoip.presentation.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.valevoip.presentation.feature.dialer.DialerScreen
import com.example.valevoip.presentation.feature.main.model.BottomBarScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    onNavigateToCall: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Dialer.route
    ) {
        composable(route = BottomBarScreen.Dialer.route) {
            DialerScreen(
                onNavigateToCall = { number ->
                    onNavigateToCall(number)
                }
            )
        }
        composable(route = BottomBarScreen.History.route) {
            Box(Modifier.fillMaxSize()) { Text("Histórico") }
        }
        composable(route = BottomBarScreen.Settings.route) {
            Box(Modifier.fillMaxSize()) { Text("Configurações") }
        }
    }
}