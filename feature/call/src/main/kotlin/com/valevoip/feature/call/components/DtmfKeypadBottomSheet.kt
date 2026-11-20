package com.valevoip.feature.call.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.DialerButton

private data class DtmfKey(
    val symbol: Char,
    val subText: String = ""
)

private val dtmfMatrix = listOf(
    listOf(DtmfKey('1', ""), DtmfKey('2', "ABC"), DtmfKey('3', "DEF")),
    listOf(DtmfKey('4', "GHI"), DtmfKey('5', "JKL"), DtmfKey('6', "MNO")),
    listOf(DtmfKey('7', "PQRS"), DtmfKey('8', "TUV"), DtmfKey('9', "WXYZ")),
    listOf(DtmfKey('*', ""), DtmfKey('0', "+"), DtmfKey('#', ""))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DtmfKeypadBottomSheet(
    dtmfInput: String,
    onDigitClick: (Char) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dtmfInput.isEmpty()) "Teclado DTMF" else dtmfInput,
                    style = if (dtmfInput.isEmpty()) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    color = if (dtmfInput.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar teclado DTMF"
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            dtmfMatrix.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowKeys.forEach { key ->
                        DialerButton(
                            symbol = key.symbol.toString(),
                            subText = key.subText,
                            modifier = Modifier.weight(1f),
                            onClick = { onDigitClick(key.symbol) }
                        )
                    }
                }
            }
        }
    }
}
