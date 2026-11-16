package com.valevoip.feature.home.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valevoip.core.designsystem.component.AlertDialogCompose
import com.valevoip.core.designsystem.component.LocalTopBarActions
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isAudioGranted = permissions[Manifest.permission.RECORD_AUDIO]
                ?: (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED)
            if (isAudioGranted) {
                startMonitoringService(context)
            }
            homeViewModel.checkBatteryOptimizationOnce()
        }
    )

    LaunchedEffect(Unit) {
        val isAudioGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (isAudioGranted) {
            startMonitoringService(context)
        }

        val hasAllPermissions = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAllPermissions) {
            homeViewModel.checkBatteryOptimizationOnce()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    val topBarActions = remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    CompositionLocalProvider(LocalTopBarActions provides topBarActions) {
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

    if (state.showBatteryOptimizationDialog) {
        AlertDialogCompose(
            dialogTitle = "Manter Ramal Conectado",
            dialogMessage = "Para receber chamadas VoIP com a tela bloqueada sem interrupções do modo de economia (Doze), permita que o Vale VoIP funcione em segundo plano sem restrições de bateria.",
            icon = Icons.Default.BatteryAlert,
            onDismissRequest = homeViewModel::dismissBatteryOptimizationDialog,
            onConfirmationClick = {
                homeViewModel.dismissBatteryOptimizationDialog()
                requestIgnoreBatteryOptimizations(context)
            },
            onDismissClick = homeViewModel::dismissBatteryOptimizationDialog
        )
    }
}

private fun startMonitoringService(context: Context) {
    VoipForegroundService.start(context)
}

@SuppressLint("BatteryLife")
private fun requestIgnoreBatteryOptimizations(context: Context) {
    val requestIntent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}")
    )
    try {
        context.startActivity(requestIntent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        runCatching { context.startActivity(fallbackIntent) }
    }
}
