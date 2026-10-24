package com.valevoip.feature.history

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.valevoip.core.designsystem.component.AlertDialogCompose

@Composable
internal fun HistoryOverflowMenu(
    hasItems: Boolean,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { isMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Mais opções"
            )
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Limpar histórico",
                        color = if (hasItems) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar histórico",
                        tint = if (hasItems) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                enabled = hasItems,
                onClick = {
                    isMenuExpanded = false
                    showConfirmDialog = true
                }
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialogCompose(
            dialogTitle = "Limpar histórico",
            dialogMessage = "Deseja realmente apagar todo o histórico de chamadas? Esta ação não pode ser desfeita.",
            icon = Icons.Default.Delete,
            onDismissRequest = { showConfirmDialog = false },
            onConfirmationClick = {
                showConfirmDialog = false
                onClearHistory()
            },
            onDismissClick = { showConfirmDialog = false }
        )
    }
}
