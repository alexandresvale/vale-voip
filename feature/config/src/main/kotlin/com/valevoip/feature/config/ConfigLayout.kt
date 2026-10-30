package com.valevoip.feature.config

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.valevoip.core.designsystem.component.AlertDialogCompose
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.VoipSettings

@Composable
internal fun ConfigLayout(
    state: ConfigUiState,
    onAction: (ConfigUiAction) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Card Superior - Ramal
        AccountInfoCard(
            username = state.username,
            domain = state.domain,
            registrationState = state.registrationState
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seção Áudio & Chamadas
        SectionHeader(title = "ÁUDIO & CHAMADAS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ValeVoipTheme.extendedColors.cardBackground),
            border = BorderStroke(1.dp, ValeVoipTheme.extendedColors.cardBorder)
        ) {
            Column {
                SettingsSwitchRow(
                    icon = Icons.Default.GraphicEq,
                    iconBgColor = ValeVoipTheme.extendedColors.blueBg,
                    iconTint = ValeVoipTheme.extendedColors.blueFg,
                    title = "Redução de Ruído & Eco",
                    subtitle = "Melhora a clareza da voz em chamadas",
                    checked = state.settings.echoCancellationEnabled,
                    onCheckedChange = { onAction(ConfigUiAction.SetEchoCancellation(it)) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = ValeVoipTheme.extendedColors.cardBorder
                )

                SettingsSwitchRow(
                    icon = Icons.Default.Vibration,
                    iconBgColor = ValeVoipTheme.extendedColors.orangeBg,
                    iconTint = ValeVoipTheme.extendedColors.orangeFg,
                    title = "Vibrar ao Tocar",
                    subtitle = "Vibrar o telefone nas chamadas recebidas",
                    checked = state.settings.vibrateOnIncomingCall,
                    onCheckedChange = { onAction(ConfigUiAction.SetVibrate(it)) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = ValeVoipTheme.extendedColors.cardBorder
                )

                SettingsActionRow(
                    icon = Icons.Default.Mic,
                    iconBgColor = MaterialTheme.colorScheme.primary,
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    title = "Testar Microfone e Alto-falante",
                    subtitle = "Grave sua voz e escute para validar o áudio",
                    onClick = { onAction(ConfigUiAction.OpenAudioTest) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção Rede & Conectividade
        SectionHeader(title = "REDE & CONECTIVIDADE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ValeVoipTheme.extendedColors.cardBackground),
            border = BorderStroke(1.dp, ValeVoipTheme.extendedColors.cardBorder)
        ) {
            SettingsSwitchRow(
                icon = Icons.Default.Wifi,
                iconBgColor = ValeVoipTheme.extendedColors.blueBg,
                iconTint = ValeVoipTheme.extendedColors.blueFg,
                title = "Usar Apenas Wi-Fi",
                subtitle = "Restringe chamadas para economizar dados móveis",
                checked = state.settings.useWifiOnly,
                onCheckedChange = { onAction(ConfigUiAction.SetWifiOnly(it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção Armazenamento
        SectionHeader(title = "ARMAZENAMENTO")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ValeVoipTheme.extendedColors.cardBackground),
            border = BorderStroke(1.dp, ValeVoipTheme.extendedColors.cardBorder)
        ) {
            SettingsActionRow(
                icon = Icons.Default.DeleteOutline,
                iconBgColor = ValeVoipTheme.extendedColors.redBg,
                iconTint = ValeVoipTheme.extendedColors.redFg,
                title = "Limpar Histórico de Chamadas",
                titleColor = ValeVoipTheme.extendedColors.redFg,
                subtitle = "${state.callHistoryCount} registros armazenados",
                onClick = { onAction(ConfigUiAction.OpenClearHistoryDialog) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção Suporte & Sobre
        SectionHeader(title = "SUPORTE & SOBRE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ValeVoipTheme.extendedColors.cardBackground),
            border = BorderStroke(1.dp, ValeVoipTheme.extendedColors.cardBorder)
        ) {
            SettingsActionRow(
                icon = Icons.Default.Share,
                iconBgColor = ValeVoipTheme.extendedColors.blueBg,
                iconTint = ValeVoipTheme.extendedColors.blueFg,
                title = "Diagnóstico para Suporte",
                titleColor = MaterialTheme.colorScheme.onSurface,
                subtitle = "Compartilhar status técnico com a equipe ValeVoip",
                onClick = { onAction(ConfigUiAction.ShareDiagnosticReport) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialog de Confirmação para Limpar Histórico
    if (state.showClearHistoryDialog) {
        AlertDialogCompose(
            dialogTitle = "Limpar Histórico",
            dialogMessage = "Deseja realmente apagar todos os ${state.callHistoryCount} registros de chamadas armazenados? Esta ação não pode ser desfeita.",
            icon = Icons.Default.Warning,
            onDismissRequest = { onAction(ConfigUiAction.DismissClearHistoryDialog) },
            onConfirmationClick = { onAction(ConfigUiAction.ConfirmClearHistory) },
            onDismissClick = { onAction(ConfigUiAction.DismissClearHistoryDialog) }
        )
    }

    // BottomSheet para Teste de Microfone e Alto-falante
    if (state.showAudioTestBottomSheet) {
        AudioTestBottomSheet(
            isTesting = state.isTestingAudio,
            onStartTest = { onAction(ConfigUiAction.StartAudioTest) },
            onStopTest = { onAction(ConfigUiAction.StopAudioTest) },
            onDismiss = { onAction(ConfigUiAction.CloseAudioTest) }
        )
    }
}

@Composable
private fun AccountInfoCard(
    username: String,
    domain: String,
    registrationState: SipRegistrationState,
) {
    val isOnline = registrationState is SipRegistrationState.Ok
    val badgeBg = if (isOnline) {
        ValeVoipTheme.extendedColors.greenBg
    } else {
        ValeVoipTheme.extendedColors.redBg
    }
    val badgeText = if (isOnline) {
        ValeVoipTheme.extendedColors.greenFg
    } else {
        ValeVoipTheme.extendedColors.redFg
    }
    val badgeLabel = if (isOnline) "• Online" else "• Offline"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ramal conectado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
private fun ConfigLayoutPreview() {
    ValeVoipTheme {
        ConfigLayout(
            state = ConfigUiState(
                username = "1001",
                domain = "sip.valevoip.com.br",
                registrationState = SipRegistrationState.Ok,
                settings = VoipSettings(),
                callHistoryCount = 15
            ),
            onAction = {}
        )
    }
}
