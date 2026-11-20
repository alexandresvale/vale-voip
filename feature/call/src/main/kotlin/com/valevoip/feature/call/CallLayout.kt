package com.valevoip.feature.call

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.ValeVoipActionButton
import com.valevoip.core.designsystem.component.ValeVoipActionButtonType
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.AudioRouteType
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.feature.call.components.AudioRouteBottomSheet
import com.valevoip.feature.call.components.DtmfKeypadBottomSheet

@Composable
internal fun CallLayout(
    state: CallUiState,
    onEvent: (CallUiEvent) -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CABEÇALHO E AVATAR (Reutilizado para todos os estados) ---
            Spacer(modifier = Modifier.weight(0.15f))

            // Texto de Status Inteligente
            val statusText = when (state.callStatus) {
                CallStatus.IDLE,
                CallStatus.DIALING -> "Conectando..."
                CallStatus.RINGING -> "Chamando..."
                CallStatus.INCOMING -> "Chamada Recebida"
                CallStatus.ACTIVE -> state.formattedDuration
                CallStatus.PAUSED -> {
                    if (state.duration > 0L) {
                        "Em espera • ${state.formattedDuration}"
                    } else {
                        "Em espera"
                    }
                }
                CallStatus.ENDED -> "Encerrado"
            }

            val isHighlightedStatus = state.callStatus == CallStatus.INCOMING ||
                state.callStatus == CallStatus.PAUSED

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isHighlightedStatus) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            val showCodecBadge = !state.activeCodecLabel.isNullOrBlank() &&
                (state.callStatus == CallStatus.ACTIVE || state.callStatus == CallStatus.PAUSED)

            if (showCodecBadge) {
                Spacer(modifier = Modifier.height(8.dp))
                ActiveCodecBadge(codecLabel = state.activeCodecLabel.orEmpty())
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Avatar
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isUnknownContact = state.contactName.isBlank() ||
                state.contactName.equals("Desconhecido", ignoreCase = true)

            if (!isUnknownContact) {
                Text(
                    text = state.contactName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = state.contactNumber,
                style = if (isUnknownContact) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = if (isUnknownContact) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // --- ÁREA DE BOTÕES DINÂMICA ---
            if (state.callStatus == CallStatus.INCOMING) {
                IncomingCallControls(onEvent)
            } else {
                ActiveCallControls(state, onEvent)
            }
        }
    }

    if (state.isKeypadVisible) {
        DtmfKeypadBottomSheet(
            dtmfInput = state.dtmfInput,
            onDigitClick = { digit -> onEvent(CallUiEvent.OnDtmfDigit(digit)) },
            onDismiss = { onEvent(CallUiEvent.OnDismissKeypad) }
        )
    }

    if (state.isAudioRouteSheetVisible) {
        AudioRouteBottomSheet(
            routes = state.availableAudioRoutes,
            activeRouteId = state.activeAudioRoute?.id,
            onSelectRoute = { routeId -> onEvent(CallUiEvent.OnSelectAudioRoute(routeId)) },
            onDismiss = { onEvent(CallUiEvent.OnDismissAudioRouteSheet) }
        )
    }
}

@Composable
private fun ActiveCodecBadge(codecLabel: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = codecLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IncomingCallControls(onEvent: (CallUiEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ValeVoipActionButton(
            icon = Icons.Default.CallEnd,
            contentDescription = "Rejeitar",
            buttonType = ValeVoipActionButtonType.DESTRUCTIVE,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )

        ValeVoipActionButton(
            icon = Icons.Default.Call,
            contentDescription = "Atender",
            buttonType = ValeVoipActionButtonType.ACCEPT,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnAnswer) }
        )
    }
}

@Composable
private fun ActiveCallControls(state: CallUiState, onEvent: (CallUiEvent) -> Unit) {
    val audioButtonVisual = resolveAudioButtonVisual(
        activeRoute = state.activeAudioRoute,
        isSpeakerOn = state.isSpeakerOn
    )
    val isInConversation = state.callStatus == CallStatus.ACTIVE ||
        state.callStatus == CallStatus.PAUSED
    val isPreConnect = state.callStatus == CallStatus.DIALING ||
        state.callStatus == CallStatus.RINGING
    val isOnHold = state.callStatus == CallStatus.PAUSED

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isInConversation) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValeVoipActionButton(
                    icon = Icons.Default.Dialpad,
                    label = "Teclado",
                    buttonType = ValeVoipActionButtonType.DEFAULT,
                    modifier = Modifier.size(64.dp),
                    onClick = { onEvent(CallUiEvent.OnShowKeypad) }
                )
                ValeVoipActionButton(
                    icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mudo",
                    isActive = state.isMuted,
                    buttonType = ValeVoipActionButtonType.TOGGLE,
                    modifier = Modifier.size(64.dp),
                    onClick = { onEvent(CallUiEvent.OnToggleMute) }
                )
                ValeVoipActionButton(
                    icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                    label = if (isOnHold) "Retomar" else "Espera",
                    isActive = isOnHold,
                    buttonType = ValeVoipActionButtonType.TOGGLE,
                    modifier = Modifier.size(64.dp),
                    onClick = { onEvent(CallUiEvent.OnToggleHold) }
                )
                ValeVoipActionButton(
                    icon = audioButtonVisual.icon,
                    label = audioButtonVisual.label,
                    isActive = audioButtonVisual.isActive,
                    buttonType = ValeVoipActionButtonType.TOGGLE,
                    modifier = Modifier.size(64.dp),
                    onClick = { onEvent(CallUiEvent.OnAudioRouteClick) }
                )
            }
        } else if (isPreConnect) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValeVoipActionButton(
                    icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mudo",
                    isActive = state.isMuted,
                    buttonType = ValeVoipActionButtonType.TOGGLE,
                    onClick = { onEvent(CallUiEvent.OnToggleMute) }
                )
                ValeVoipActionButton(
                    icon = audioButtonVisual.icon,
                    label = audioButtonVisual.label,
                    isActive = audioButtonVisual.isActive,
                    buttonType = ValeVoipActionButtonType.TOGGLE,
                    onClick = { onEvent(CallUiEvent.OnAudioRouteClick) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ValeVoipActionButton(
            icon = Icons.Default.CallEnd,
            contentDescription = "Desligar",
            buttonType = ValeVoipActionButtonType.DESTRUCTIVE,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )
    }
}

private data class AudioButtonVisual(
    val icon: ImageVector,
    val label: String,
    val isActive: Boolean
)

private fun resolveAudioButtonVisual(
    activeRoute: AudioRoute?,
    isSpeakerOn: Boolean
): AudioButtonVisual {
    return when (activeRoute?.type) {
        AudioRouteType.BLUETOOTH -> AudioButtonVisual(
            icon = Icons.Default.BluetoothAudio,
            label = "Bluetooth",
            isActive = true
        )

        AudioRouteType.WIRED_HEADSET -> AudioButtonVisual(
            icon = Icons.Default.Headset,
            label = "Fone",
            isActive = true
        )

        AudioRouteType.SPEAKER -> AudioButtonVisual(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = "Viva-voz",
            isActive = true
        )

        AudioRouteType.EARPIECE -> AudioButtonVisual(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = "Viva-voz",
            isActive = false
        )

        null -> AudioButtonVisual(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = "Viva-voz",
            isActive = isSpeakerOn
        )
    }
}

private class CallStateProvider : PreviewParameterProvider<CallUiState> {
    override val values = sequenceOf(
        CallUiState(
            contactName = "Maria Silva",
            contactNumber = "valevoip",
            callStatus = CallStatus.DIALING
        ),
        CallUiState(
            contactName = "João Santos",
            contactNumber = "alexandreskt16",
            callStatus = CallStatus.INCOMING
        ),
        CallUiState(
            contactName = "Suporte Técnico",
            contactNumber = "0800 123 456",
            callStatus = CallStatus.ACTIVE,
            formattedDuration = "04:20",
            isSpeakerOn = true
        ),
        CallUiState(
            contactName = "Suporte Técnico",
            contactNumber = "0800 123 456",
            callStatus = CallStatus.PAUSED,
            duration = 260L,
            formattedDuration = "04:20"
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Light Mode")
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun CallLayoutPreview(
    @PreviewParameter(CallStateProvider::class) state: CallUiState
) {
    ValeVoipTheme {
        CallLayout(
            state = state,
            onEvent = {}
        )
    }
}
