package com.valevoip.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
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
    onNavigateToCall: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    HistoryContent(
        state = state,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onFilterMissedChange = viewModel::updateFilterMissed,
        onClearHistory = viewModel::clearHistory,
        onNavigateToCall = onNavigateToCall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterMissedChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateToCall: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Histórico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onClearHistory) {
                Text(
                    text = "Limpar",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Buscar por nome ou número") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentedButton(
                text = "Todas",
                isSelected = !state.filterMissed,
                onClick = { onFilterMissedChange(false) },
                modifier = Modifier.weight(1f)
            )
            SegmentedButton(
                text = "Perdidas",
                isSelected = state.filterMissed,
                onClick = { onFilterMissedChange(true) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            if (state.isLoading && state.allItems.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.allItems.isEmpty()) {
                Text("Erro: ${state.errorMessage}", Modifier.align(Alignment.Center))
            } else if (state.filteredItems.isEmpty()) {
                Text(
                    text = if (state.searchQuery.isNotEmpty()) "Nenhuma chamada encontrada" else "Nenhuma chamada recente",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HistoryList(
                    items = state.filteredItems,
                    onItemClick = onNavigateToCall
                )
            }
        }
    }
}

@Composable
private fun SegmentedButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun HistoryList(items: List<CallHistoryItem>, onItemClick: (String) -> Unit) {
    // Group by formatted date
    val groupedItems = items.groupBy { formatGroupDate(it.timestamp) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedItems.forEach { (dateStr, calls) ->
            item {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            items(calls) { item ->
                HistoryItemRow(item = item, onClick = { onItemClick(item.remoteAddress) })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun HistoryItemRow(item: CallHistoryItem, onClick: () -> Unit) {
    val callTypeInfo = getCallTypeInfo(item)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(callTypeInfo.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = callTypeInfo.icon,
                contentDescription = callTypeInfo.label,
                tint = callTypeInfo.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayName ?: item.remoteAddress,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${callTypeInfo.label} · ${formatTimeOnly(item.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right side (Time again or just spacing as requested)
        Text(
            text = formatTimeOnly(item.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class CallTypeInfo(
    val label: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
)

private fun getCallTypeInfo(item: CallHistoryItem): CallTypeInfo {
    return when {
        item.status == CallHistoryStatus.DECLINED -> CallTypeInfo(
            label = "Recusada",
            icon = Icons.Default.Block,
            backgroundColor = Color(0xFFE3F2FD), // Light Blue
            iconColor = Color(0xFF1565C0) // Dark Blue
        )

        item.direction == CallDirection.INCOMING && item.status == CallHistoryStatus.MISSED -> CallTypeInfo(
            label = "Perdida",
            icon = Icons.AutoMirrored.Filled.CallMissed,
            backgroundColor = Color(0xFFFFEBEE), // Light Red
            iconColor = Color(0xFFC62828) // Dark Red
        )

        item.direction == CallDirection.INCOMING -> CallTypeInfo(
            label = "Recebida",
            icon = Icons.AutoMirrored.Filled.CallReceived,
            backgroundColor = Color(0xFFF5F5F5), // Light Grey
            iconColor = Color(0xFF616161) // Dark Grey
        )

        else -> CallTypeInfo( // OUTGOING
            label = "Efetuada",
            icon = Icons.AutoMirrored.Filled.CallMade,
            backgroundColor = Color(0xFFE8F5E9), // Light Green
            iconColor = Color(0xFF2E7D32) // Dark Green
        )
    }
}

private fun formatGroupDate(ts: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(ts))
}

private fun formatTimeOnly(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

internal class HistoryUiStateProvider : PreviewParameterProvider<HistoryUiState> {
    override val values = sequenceOf(
        HistoryUiState(
            allItems = emptyList(),
            filteredItems = emptyList()
        ),
        HistoryUiState(
            allItems = dummyItems,
            filteredItems = dummyItems
        )
    )
}

private val dummyItems = listOf(
    CallHistoryItem(
        id = "1",
        remoteAddress = "alexandresvale",
        displayName = "alexandresvale",
        direction = CallDirection.OUTGOING,
        status = CallHistoryStatus.SUCCESS,
        timestamp = System.currentTimeMillis(),
        durationSeconds = 120
    ),
    CallHistoryItem(
        id = "2",
        remoteAddress = "alexandresvale",
        displayName = "alexandresvale",
        direction = CallDirection.INCOMING,
        status = CallHistoryStatus.DECLINED,
        timestamp = System.currentTimeMillis() - 60000,
        durationSeconds = 0
    ),
    CallHistoryItem(
        id = "3",
        remoteAddress = "alexandresvale",
        displayName = "alexandresvale",
        direction = CallDirection.INCOMING,
        status = CallHistoryStatus.MISSED,
        timestamp = System.currentTimeMillis() - 86400000,
        durationSeconds = 0
    ),
    CallHistoryItem(
        id = "4",
        remoteAddress = "alexandresvale",
        displayName = "alexandresvale",
        direction = CallDirection.INCOMING,
        status = CallHistoryStatus.SUCCESS,
        timestamp = System.currentTimeMillis() - 86400000 - 60000,
        durationSeconds = 120
    )
)

@Preview(showBackground = true)
@Composable
internal fun HistoryContentPreview(
    @PreviewParameter(HistoryUiStateProvider::class) state: HistoryUiState
) {
    ValeVoipTheme {
        HistoryContent(
            state = state,
            onSearchQueryChange = {},
            onFilterMissedChange = {},
            onClearHistory = {},
            onNavigateToCall = {}
        )
    }
}