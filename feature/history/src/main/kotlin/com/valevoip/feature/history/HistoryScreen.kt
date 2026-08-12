package com.valevoip.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToCall: (String) -> Unit // Callback para rediscar ao clicar
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    HistoryContent(
        state = state,
        onNavigateToCall = onNavigateToCall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onNavigateToCall: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (state) {
            is HistoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is HistoryUiState.Empty -> Text("Nenhuma chamada recente", Modifier.align(Alignment.Center))
            is HistoryUiState.Error -> Text("Erro: ${state.message}", Modifier.align(Alignment.Center))
            is HistoryUiState.Success -> {
                HistoryList(
                    items = state.items,
                    onItemClick = onNavigateToCall
                )
            }
        }
    }
}

@Composable
internal fun HistoryList(items: List<CallHistoryItem?>, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            item ?: return@items
            HistoryItemRow(item = item, onClick = { onItemClick(item.remoteAddress) })
            HorizontalDivider()
        }
    }
}

@Composable
internal fun HistoryItemRow(item: CallHistoryItem, onClick: () -> Unit) {
    val icon = when (item.direction) {
        CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
        CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
    }

    val color = when (item.status) {
        CallHistoryStatus.MISSED -> Color.Red
        CallHistoryStatus.DECLINED -> Color.Gray
        CallHistoryStatus.SUCCESS -> if (item.direction == CallDirection.INCOMING) Color.Blue else Color.Green
    }

    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        },
        headlineContent = {
            Text(text = item.displayName ?: item.remoteAddress, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Column {
                if (!item.displayName.isNullOrBlank()) {
                    Text(text = item.remoteAddress, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = formatTimestamp(item.timestamp), // Implementar formatador simples
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        trailingContent = {
            Text(text = formatDuration(item.durationSeconds))
        }
    )
}

// Função utilitária simples
fun formatDuration(seconds: Int): String {
    if (seconds == 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

internal class HistoryUiStateProvider : PreviewParameterProvider<HistoryUiState> {
    override val values = sequenceOf(
        HistoryUiState.Error(message = "Erro ao buscar histórico de chamadas"),
        HistoryUiState.Empty,
        HistoryUiState.Loading,
        HistoryUiState.Success(
            items = listOf(
                CallHistoryItem(
                    id = "123",
                    remoteAddress = "123456789",
                    displayName = "João Silva",
                    direction = CallDirection.INCOMING,
                    status = CallHistoryStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = 120
                ),
                CallHistoryItem(
                    id = "123",
                    remoteAddress = "123456789",
                    displayName = "João Silva",
                    direction = CallDirection.OUTGOING,
                    status = CallHistoryStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = 120
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
internal fun HistoryContentPreview(
    @PreviewParameter(HistoryUiStateProvider::class) state: HistoryUiState
) {
    ValeVoipTheme {
        HistoryContent(
            state = state,
            onNavigateToCall = {}
        )
    }
}