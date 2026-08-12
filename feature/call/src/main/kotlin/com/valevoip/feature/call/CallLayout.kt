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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.ValeVoipCallButton
import com.valevoip.core.designsystem.component.ValeVoipCallButtonType
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.CallStatus

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
                CallStatus.IDLE, CallStatus.DIALING -> "Conectando..."
                CallStatus.RINGING -> "Chamando..."
                CallStatus.INCOMING -> "Chamada Recebida"
                CallStatus.ACTIVE -> state.formattedDuration
                CallStatus.ENDED -> "Encerrado"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = if (state.callStatus == CallStatus.INCOMING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // Avatar (Pode adicionar uma animação de "pulsar" se for INCOMING)
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
            Text(
                text = state.contactName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = state.contactNumber,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // --- ÁREA DE BOTÕES DINÂMICA ---
            if (state.callStatus == CallStatus.INCOMING) {
                // LAYOUT DE CHAMADA RECEBIDA (Dois Botões)
                IncomingCallControls(onEvent)
            } else {
                // LAYOUT DE CHAMADA ATIVA/EFETUADA (Controles + Desligar)
                ActiveCallControls(state, onEvent)
            }
        }
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
        ValeVoipCallButton(
            icon = Icons.Default.CallEnd,
            contentDescription = "Rejeitar",
            buttonType = ValeVoipCallButtonType.DESTRUCTIVE,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )

        ValeVoipCallButton(
            icon = Icons.Default.Call,
            contentDescription = "Atender",
            buttonType = ValeVoipCallButtonType.ACCEPT,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnAnswer) }
        )
    }
}

@Composable
private fun ActiveCallControls(state: CallUiState, onEvent: (CallUiEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.callStatus == CallStatus.ACTIVE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValeVoipCallButton(
                    icon = Icons.Default.Dialpad,
                    label = "Teclado",
                    buttonType = ValeVoipCallButtonType.DEFAULT,
                    onClick = { onEvent(CallUiEvent.OnShowKeypad) }
                )
                ValeVoipCallButton(
                    icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mudo",
                    isActive = state.isMuted,
                    buttonType = ValeVoipCallButtonType.TOGGLE,
                    onClick = { onEvent(CallUiEvent.OnToggleMute) }
                )
                ValeVoipCallButton(
                    icon = Icons.AutoMirrored.Default.VolumeUp,
                    label = "Viva-voz",
                    isActive = state.isSpeakerOn,
                    buttonType = ValeVoipCallButtonType.TOGGLE,
                    onClick = { onEvent(CallUiEvent.OnToggleSpeaker) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ValeVoipCallButton(
            icon = Icons.Default.CallEnd,
            contentDescription = "Desligar",
            buttonType = ValeVoipCallButtonType.DESTRUCTIVE,
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )
    }
}

private class CallStateProvider : PreviewParameterProvider<CallUiState> {
    override val values = sequenceOf(
        // Caso 1: Chamada Efetuada (Discando)
        CallUiState(
            contactName = "Maria Silva",
            contactNumber = "(11) 99999-1234",
            callStatus = CallStatus.DIALING
        ),
        // Caso 2: Chamada Recebida (Alguém me ligando)
        CallUiState(
            contactName = "João Santos",
            contactNumber = "(85) 98888-7777",
            callStatus = CallStatus.INCOMING // Importante para testar a UI de atender
        ),
        // Caso 3: Chamada Ativa (Falando)
        CallUiState(
            contactName = "Suporte Técnico",
            contactNumber = "0800 123 456",
            callStatus = CallStatus.ACTIVE,
            formattedDuration = "04:20",
            isSpeakerOn = true
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