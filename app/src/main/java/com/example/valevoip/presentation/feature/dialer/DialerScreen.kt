package com.example.valevoip.presentation.feature.dialer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme

@Composable
fun DialerScreen(
    viewModel: DialerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DialerLayout(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}


@Preview
@Composable
fun DialerScreenPreview() {
    ValeVoipTheme(darkTheme = false) {
        DialerScreen()
    }
}