package com.example.valevoip.presentation.feature.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.valevoip.presentation.feature.dialer.model.KeypadUtil
import com.example.valevoip.presentation.ui.componet.DialerButton
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme


@Composable
fun DialerLayout(
    uiState: DialerUiState,
    onEvent: (DialerUiEvent) -> Unit,
) {
    val textStyle = when {
        uiState.number.length > 18 -> MaterialTheme.typography.titleMedium
        uiState.number.length > 14 -> MaterialTheme.typography.headlineSmall
        else -> MaterialTheme.typography.displaySmall
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Área do Display
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.number,
                style = textStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            )
            // Botão Backspace (Só aparece se tiver número)
            if (uiState.number.isNotEmpty()) {
                BackspaceButton(
                    onClick = { onEvent(DialerUiEvent.OnBackspace) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        // --- ÁREA DO TECLADO E CHAMADA (70% da tela) ---
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // O Grid Numérico
            DialerKeypad(
                modifier = Modifier.weight(1f),
                onDigitClick = { digit -> onEvent(DialerUiEvent.OnDigitClick(digit)) },
                onLongClickZero = { onEvent(DialerUiEvent.OnLongClickZero) }
            )
            // O Botão de Chamar (Fica na base do teclado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CallButton(onClick = {
                    onEvent(DialerUiEvent.OnCallClick)
                })
            }
        }
    }
}

@Composable
fun DialerKeypad(
    modifier: Modifier = Modifier,
    onDigitClick: (String) -> Unit,
    onLongClickZero: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        KeypadUtil.matrix.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                rowKeys.forEach { key ->
                    val longClickAction = if (key.digit == "0") onLongClickZero else null
                    DialerButton(
                        symbol = key.digit,
                        subText = key.letters,
                        modifier = Modifier.weight(1f),
                        onLongClick = longClickAction,
                        onClick = { onDigitClick(key.digit) }
                    )
                }
            }
        }
    }
}

@Composable
fun BackspaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace, // Ícone padrão do Android
            contentDescription = "Apagar",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun CallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), // Verde Telefone
        modifier = modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Chamar",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview
@Composable
fun DialerLayoutPreview() {
    ValeVoipTheme(darkTheme = false) {
        DialerLayout(
            uiState = DialerUiState(),
            onEvent = {}
        )
    }
}