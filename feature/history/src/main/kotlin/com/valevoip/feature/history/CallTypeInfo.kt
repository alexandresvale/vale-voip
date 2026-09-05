package com.valevoip.feature.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus

internal data class CallTypeInfo(
    val label: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
) {
    companion object {
        @Composable
        fun from(item: CallHistoryItem): CallTypeInfo {
            return when {
                item.status == CallHistoryStatus.DECLINED -> CallTypeInfo(
                    label = "Recusada",
                    icon = Icons.Default.Block,
                    backgroundColor = ValeVoipTheme.extendedColors.blueBg,
                    iconColor = ValeVoipTheme.extendedColors.blueFg
                )

                item.direction == CallDirection.INCOMING && item.status == CallHistoryStatus.MISSED -> CallTypeInfo(
                    label = "Perdida",
                    icon = Icons.AutoMirrored.Filled.CallMissed,
                    backgroundColor = ValeVoipTheme.extendedColors.redBg,
                    iconColor = ValeVoipTheme.extendedColors.redFg
                )

                item.direction == CallDirection.INCOMING -> CallTypeInfo(
                    label = "Recebida",
                    icon = Icons.AutoMirrored.Filled.CallReceived,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> CallTypeInfo(
                    label = "Efetuada",
                    icon = Icons.AutoMirrored.Filled.CallMade,
                    backgroundColor = ValeVoipTheme.extendedColors.greenBg,
                    iconColor = ValeVoipTheme.extendedColors.blueFg
                )
            }
        }
    }
}
