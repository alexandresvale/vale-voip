package com.valevoip.feature.config.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.AudioCodec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioCodecsBottomSheet(
    codecs: List<AudioCodec>,
    onToggleCodec: (codecId: String, enabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AudioCodecsSheetContent(
            codecs = codecs,
            onToggleCodec = onToggleCodec
        )
    }
}

@Composable
internal fun AudioCodecsSheetContent(
    codecs: List<AudioCodec>,
    onToggleCodec: (codecId: String, enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabledCount = codecs.count { it.isEnabled }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Codecs de Áudio (SDP/RTP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selecione os codecs habilitados para negociação nas chamadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ValeVoipTheme.extendedColors.blueBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$enabledCount ativos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ValeVoipTheme.extendedColors.blueFg
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items(
                items = codecs,
                key = { it.id }
            ) { codec ->
                AudioCodecRow(
                    codec = codec,
                    onToggle = { enabled -> onToggleCodec(codec.id, enabled) }
                )
                HorizontalDivider(
                    color = ValeVoipTheme.extendedColors.cardBorder.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AudioCodecRow(
    codec: AudioCodec,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!codec.isEnabled) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = codec.mimeType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${codec.formattedClockRate} • ${codec.channels}ch",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = codec.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = codec.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun AudioCodecsSheetContentPreview() {
    val sampleCodecs = listOf(
        AudioCodec(
            id = "OPUS/48000/2",
            mimeType = "OPUS",
            clockRateHz = 48000,
            channels = 2,
            description = "Alta definição adaptativa (Banda larga)",
            isEnabled = true
        ),
        AudioCodec(
            id = "PCMU/8000/1",
            mimeType = "PCMU",
            clockRateHz = 8000,
            channels = 1,
            description = "G.711 µ-law (Padrão tradicional de telefonia)",
            isEnabled = true
        ),
        AudioCodec(
            id = "PCMA/8000/1",
            mimeType = "PCMA",
            clockRateHz = 8000,
            channels = 1,
            description = "G.711 A-law (Padrão tradicional de telefonia)",
            isEnabled = true
        ),
        AudioCodec(
            id = "G722/8000/1",
            mimeType = "G722",
            clockRateHz = 8000,
            channels = 1,
            description = "Voz em alta definição (Wideband)",
            isEnabled = false
        ),
        AudioCodec(
            id = "GSM/8000/1",
            mimeType = "GSM",
            clockRateHz = 8000,
            channels = 1,
            description = "Baixo consumo de banda (Full Rate)",
            isEnabled = false
        )
    )

    ValeVoipTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AudioCodecsSheetContent(
                codecs = sampleCodecs,
                onToggleCodec = { _, _ -> }
            )
        }
    }
}
