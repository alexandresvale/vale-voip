package com.example.valevoip.presentation.feature.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.CallStatus
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.callStatus) {
        if (state.callStatus == CallStatus.ENDED) {
            delay(1000)
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