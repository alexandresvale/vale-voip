package com.valevoip.app.presentation.feature.call

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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.valevoip.domain.model.CallStatus
import com.valevoip.app.presentation.ui.componet.CallActionButton
import com.valevoip.app.presentation.ui.theme.ValeVoipTheme
import com.valevoip.app.presentation.ui.theme.primaryLight

@Composable
fun CallLayout(
    state: CallUiState,
    onEvent: (CallUiEvent) -> Unit
) {
    Scaffold(containerColor = Color.White) { padding ->
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
                color = if (state.callStatus == CallStatus.INCOMING) MaterialTheme.colorScheme.primary else Color.Gray
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // Avatar (Pode adicionar uma animação de "pulsar" se for INCOMING)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = state.contactName, style = MaterialTheme.typography.headlineMedium)
            Text(text = state.contactNumber, style = MaterialTheme.typography.titleLarge, color = Color.Gray)

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
fun IncomingCallControls(onEvent: (CallUiEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallActionButton(
            icon = Icons.Default.CallEnd,
            iconColor = Color.White,
            iconBackground = Color(0xFFD32F2F),
            contentDescription = "Rejeitar",
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )

        CallActionButton(
            icon = Icons.Default.Call,
            iconColor = Color.White,
            iconBackground = Color(0xFF2E7D32),
            contentDescription = "Atender",
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnAnswer) }
        )
    }
}

@Composable
fun ActiveCallControls(state: CallUiState, onEvent: (CallUiEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.callStatus == CallStatus.ACTIVE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallActionButton(
                    icon = Icons.Default.Dialpad,
                    iconColor = Color.White,
                    iconBackground = primaryLight,
                    label = "Teclado",
                    onClick = { onEvent(CallUiEvent.OnShowKeypad) }
                )
                CallActionButton(
                    icon = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    iconColor = Color.White,
                    iconBackground = primaryLight,
                    label = "Mudo",
                    isActive = state.isMuted,
                    onClick = { onEvent(CallUiEvent.OnToggleMute) }
                )
                CallActionButton(
                    icon = Icons.AutoMirrored.Default.VolumeUp,
                    iconColor = Color.White,
                    iconBackground = primaryLight,
                    label = "Viva-voz",
                    isActive = state.isSpeakerOn,
                    onClick = { onEvent(CallUiEvent.OnToggleSpeaker) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        CallActionButton(
            icon = Icons.Default.CallEnd,
            iconColor = Color.White,
            iconBackground = Color(0xFFD32F2F),
            contentDescription = "Desligar",
            modifier = Modifier.size(72.dp),
            onClick = { onEvent(CallUiEvent.OnHangup) }
        )
    }
}

class CallStateProvider : PreviewParameterProvider<CallUiState> {
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CallLayoutPreview(
    @PreviewParameter(CallStateProvider::class) state: CallUiState
) {
    ValeVoipTheme {
        CallLayout(
            state = state,
            onEvent = {}
        )
    }
}