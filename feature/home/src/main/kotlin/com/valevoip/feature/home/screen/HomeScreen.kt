package com.valevoip.feature.home.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valevoip.core.designsystem.util.SystemBarsController
import com.valevoip.core.navigation.navigateToBottomBarRoute
import com.valevoip.core.telecom.service.VoipForegroundService
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
    onNavigateToCall: (String) -> Unit,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    SystemBarsController(useDarkIcons = !isSystemInDarkTheme())

    val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            if (isAudioGranted) {
                startMonitoringService(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasAllPermissions = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAllPermissions) {
            startMonitoringService(context)
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    LaunchedEffect(homeViewModel) {
        homeViewModel.navigationChannel.collect { remoteNumber ->
            onNavigateToCall(remoteNumber)
        }
    }

    HomeLayout(
        state = state,
        currentDestination = currentDestination,
        onBottomItemClick = { screen ->
            bottomNavController.navigateToBottomBarRoute(screen.route)
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
    val intent = Intent(context, VoipForegroundService::class.java).apply {
        action = VoipForegroundService.ACTION_START_MONITORING
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
