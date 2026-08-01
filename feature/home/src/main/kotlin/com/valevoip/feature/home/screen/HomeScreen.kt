package com.valevoip.feature.home.screen

//import com.valevoip.app.core.service.CallService
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valevoip.feature.home.HomeNavGraph
import com.valevoip.feature.home.HomeViewModel
import com.valevoip.feature.home.model.BottomBarScreen

val bottomNavItems = listOf(
    BottomBarScreen.Dialer,
    BottomBarScreen.History,
    BottomBarScreen.Settings
)

@Composable
internal fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        startMonitoringService(context)
    }

    HomeLayout(
        state = state,
        currentRoute = currentRoute,
        onBottomItemClick = { screen ->
            bottomNavController.navigate(screen.route) {
                popUpTo(bottomNavController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                HomeNavGraph(
                    navController = bottomNavController,
                    nestedGraph = nestedGraph
                )
            }
        }
    )
}

private fun startMonitoringService(context: Context) {
    /*val intent = Intent(context, CallService::class.java).apply {
        action = CallService.ACTIONS.START_MONITORING
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }*/
}
