package com.valevoip.feature.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallStatus
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun CallScreen(
    viewModel: CallViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onEvent(CallUiEvent.OnPermissionGranted)
            } else {
                // Sem microfone não tem chamada! Encerra a tela.
                viewModel.onEvent(CallUiEvent.OnHangup)
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.onEvent(CallUiEvent.OnPermissionGranted)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state.callStatus) {
        if (state.callStatus == CallStatus.ENDED) {
            delay(1000.milliseconds)
            onNavigateBack()
        }
    }

    CallLayout(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Preview
@Composable
fun CallScreenPreview() {
    ValeVoipTheme(darkTheme = false) {
        CallScreen(
            onNavigateBack = {}
        )
    }
}