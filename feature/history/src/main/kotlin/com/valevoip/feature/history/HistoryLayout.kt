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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.designsystem.extension.toFormattedDate
import com.valevoip.core.designsystem.extension.toFormattedTime
import com.valevoip.core.domain.model.CallHistoryStatus
@Composable
internal fun HistoryLayout(
    state: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit
) {
    if (state.selectedCallDetails != null) {
        HistoryCallDetailsBottomSheet(
            item = state.selectedCallDetails,
            onDismiss = { onAction(HistoryUiAction.DismissCallDetails) },
            onCall = { number ->
                onAction(HistoryUiAction.DismissCallDetails)
                onAction(HistoryUiAction.CallContact(number))
            },
            onCopy = { number ->
                onAction(HistoryUiAction.CopyNumber(number))
                onAction(HistoryUiAction.DismissCallDetails)
            },
            onDelete = { item ->
                onAction(HistoryUiAction.DeleteHistoryItem(item))
                onAction(HistoryUiAction.DismissCallDetails)
            }
        )
    }
    HistoryContent(state, onAction) {
        onAction(HistoryUiAction.ShowCallDetails(it))
    }
}

@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit,
    onSelectItem: (CallHistoryItem) -> Unit = {}
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
            TextButton(onClick = { onAction(HistoryUiAction.ClearHistory) }) {
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
            onValueChange = { onAction(HistoryUiAction.UpdateSearchQuery(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                onClick = { onAction(HistoryUiAction.UpdateFilterMissed(false)) },
                modifier = Modifier.weight(1f)
            )
            SegmentedButton(
                text = "Perdidas",
                isSelected = state.filterMissed,
                onClick = { onAction(HistoryUiAction.UpdateFilterMissed(true)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
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
                    onItemClick = { item -> onSelectItem(item) }
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
internal fun HistoryList(items: List<CallHistoryItem>, onItemClick: (CallHistoryItem) -> Unit) {
    // Group by formatted date
    val groupedItems = items.groupBy { it.timestamp.toFormattedDate() }

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
                HistoryItemRow(item = item, onClick = { onItemClick(item) })
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
    val callTypeInfo = CallTypeInfo.from(item)
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
                text = "${callTypeInfo.label} · ${item.timestamp.toFormattedTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right side (Time again or just spacing as requested)
        Text(
            text = item.timestamp.toFormattedTime(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
            onAction = {}
        )
    }
}
