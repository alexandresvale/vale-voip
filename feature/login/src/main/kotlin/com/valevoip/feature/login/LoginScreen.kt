package com.valevoip.feature.login

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valevoip.core.designsystem.util.SystemBarsController


@Composable
internal fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDialer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    SystemBarsController(useDarkIcons = !isSystemInDarkTheme())

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToDialer -> {
                    onNavigateToDialer()
                }

                is LoginEffect.ShowErrorSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    LoginLayout(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackBarHostState
    )
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToDialer = {}
    )
}