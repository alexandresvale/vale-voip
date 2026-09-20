package com.valevoip.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToCall: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onAction(HistoryUiAction.LoadHistory)
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HistoryUiEffect.NavigateToCall -> onNavigateToCall(effect.number)
                is HistoryUiEffect.ShowToast -> {
                    android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                }

                is HistoryUiEffect.CopyToClipboard -> {
                    val clipboard =
                        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Número SIP", effect.number)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    HistoryLayout(
        state = uiState,
        onAction = viewModel::onAction
    )
}
