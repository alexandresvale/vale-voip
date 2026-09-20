package com.valevoip.feature.history

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.ValeVoipActionButton
import com.valevoip.core.designsystem.component.ValeVoipActionButtonType
import com.valevoip.core.designsystem.extension.toFormattedDateTime
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryCallDetailsBottomSheet(
    item: CallHistoryItem,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit,
    onCopy: (String) -> Unit,
    onDelete: (CallHistoryItem) -> Unit
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        HistoryCallDetailsContent(
            item = item,
            onDismiss = onDismiss,
            onCall = onCall,
            onCopy = onCopy,
            onDelete = onDelete
        )
    }
}

@Composable
internal fun HistoryCallDetailsContent(
    item: CallHistoryItem,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit,
    onCopy: (String) -> Unit,
    onDelete: (CallHistoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Title and Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detalhes da Chamada",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        val name = item.displayName ?: item.remoteAddress
        val initial = name.firstOrNull()?.uppercase() ?: "?"
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name & Number
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = item.remoteAddress,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ValeVoipActionButton(
                icon = Icons.Default.Call,
                label = "Ligar",
                buttonType = ValeVoipActionButtonType.TONAL_ACCEPT,
                onClick = { onCall(item.remoteAddress) }
            )
            ValeVoipActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copiar",
                buttonType = ValeVoipActionButtonType.TONAL_DEFAULT,
                onClick = { onCopy(item.remoteAddress) }
            )
            ValeVoipActionButton(
                icon = Icons.Default.Delete,
                label = "Apagar",
                buttonType = ValeVoipActionButtonType.TONAL_DESTRUCTIVE,
                onClick = { onDelete(item) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details Card
        val callTypeInfo = CallTypeInfo.from(item)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            DetailRow(
                icon = callTypeInfo.icon,
                iconColor = callTypeInfo.iconColor,
                label = "Tipo de Chamada",
                value = callTypeInfo.label
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            DetailRow(
                icon = Icons.Default.Event,
                iconColor = MaterialTheme.colorScheme.primary,
                label = "Data e Hora",
                value = item.timestamp.toFormattedDateTime()
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            val durationStr = if (item.durationSeconds > 0) "${item.durationSeconds}s" else "0s"
            DetailRow(
                icon = Icons.Default.Schedule,
                iconColor = com.valevoip.core.designsystem.theme.orange,
                label = "Duração",
                value = durationStr
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            DetailRow(
                icon = Icons.Default.CellTower,
                iconColor = com.valevoip.core.designsystem.theme.purple,
                label = "Status SIP",
                value = if (item.status == CallHistoryStatus.SUCCESS) "Atendida" else callTypeInfo.label
            )
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, iconColor: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun HistoryCallDetailsContentPreview() {
    ValeVoipTheme {
        HistoryCallDetailsContent(
            item = CallHistoryItem(
                id = "1",
                remoteAddress = "sip:test@example.com",
                displayName = "João Silva",
                direction = CallDirection.OUTGOING,
                status = CallHistoryStatus.SUCCESS,
                timestamp = System.currentTimeMillis(),
                durationSeconds = 120
            ),
            onDismiss = {},
            onCall = {},
            onCopy = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HistoryCallDetailsContentPreviewDark() {
    ValeVoipTheme {
        HistoryCallDetailsContent(
            item = CallHistoryItem(
                id = "1",
                remoteAddress = "sip:test@example.com",
                displayName = "João Silva",
                direction = CallDirection.OUTGOING,
                status = CallHistoryStatus.SUCCESS,
                timestamp = System.currentTimeMillis(),
                durationSeconds = 120
            ),
            onDismiss = {},
            onCall = {},
            onCopy = {},
            onDelete = {}
        )
    }
}
