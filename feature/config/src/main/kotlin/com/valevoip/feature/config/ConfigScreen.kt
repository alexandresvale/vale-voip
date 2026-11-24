package com.valevoip.feature.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun ConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.loadHistoryCount()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAction(ConfigUiAction.RefreshBatteryOptimizationStatus)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ConfigUiEffect.ShareText -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.report)
                        putExtra(Intent.EXTRA_SUBJECT, "Diagnóstico de Suporte VoIP")
                    }
                    val chooserIntent = Intent.createChooser(sendIntent, "Compartilhar Diagnóstico").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(chooserIntent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Não foi possível abrir o compartilhador", Toast.LENGTH_SHORT).show()
                    }
                }

                is ConfigUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is ConfigUiEffect.OpenBatteryOptimizationSettings -> {
                    openBatterySettings(context, isAlreadyIgnoring = effect.isAlreadyIgnoring)
                }

                is ConfigUiEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    ConfigLayout(
        state = uiState,
        onAction = viewModel::onAction
    )
}

@SuppressLint("BatteryLife")
private fun openBatterySettings(context: Context, isAlreadyIgnoring: Boolean) {
    if (!isAlreadyIgnoring) {
        val directIntent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        try {
            context.startActivity(directIntent)
            return
        } catch (_: Exception) {
            // Fallback para a lista geral caso a ROM não suporte o diálogo direto
        }
    }

    val listIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    try {
        context.startActivity(listIntent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Não foi possível abrir as configurações de bateria do sistema",
            Toast.LENGTH_SHORT
        ).show()
    }
}
