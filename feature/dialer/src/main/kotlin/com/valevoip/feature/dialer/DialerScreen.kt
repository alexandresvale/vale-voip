package com.valevoip.feature.dialer

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.valevoip.core.designsystem.theme.ValeVoipTheme

@Composable
internal fun DialerScreen(
    viewModel: DialerViewModel = hiltViewModel(),
    onNavigateToCall: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DialerEffect.NavigateToCall -> {
                    onNavigateToCall(effect.number)
                }

                is DialerEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DialerLayout(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Preview
@Composable
internal fun DialerScreenPreview() {
    ValeVoipTheme(darkTheme = false) {
        DialerScreen(
            onNavigateToCall = {}
        )
    }
}