package com.valevoip.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.theme.ValeVoipTheme

enum class ValeVoipCallButtonType {
    DEFAULT,     // Ação comum (Teclado)
    TOGGLE,      // Ação de Liga/Desliga (Mudo, Viva-voz)
    DESTRUCTIVE, // Ação vermelha (Desligar)
    ACCEPT       // Ação verde (Atender)
}

@Composable
fun ValeVoipCallButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    label: String? = null,
    contentDescription: String? = null,
    isActive: Boolean = false,
    buttonType: ValeVoipCallButtonType = ValeVoipCallButtonType.DEFAULT,
    onClick: () -> Unit
) {
    // Lógica de cores baseada no tipo de botão e se está ativo ou não
    val backgroundColor: Color
    val iconColor: Color
    val borderColor: Color

    when (buttonType) {
        ValeVoipCallButtonType.DEFAULT -> {
            backgroundColor = MaterialTheme.colorScheme.primary
            iconColor = MaterialTheme.colorScheme.onPrimary
            borderColor = Color.Transparent
        }

        ValeVoipCallButtonType.TOGGLE -> {
            if (isActive) {
                // Ativado: Fundo preenchido com a cor primária (Azul)
                backgroundColor = MaterialTheme.colorScheme.primary
                iconColor = MaterialTheme.colorScheme.onPrimary
                borderColor = Color.Transparent
            } else {
                // Desativado (Normal): Fundo Transparente (se adapta ao Dark/Light mode), Ícone e Borda Azul
                backgroundColor = Color.Transparent
                iconColor = MaterialTheme.colorScheme.primary
                borderColor = MaterialTheme.colorScheme.primary
            }
        }

        ValeVoipCallButtonType.DESTRUCTIVE -> {
            backgroundColor = MaterialTheme.colorScheme.error
            iconColor = MaterialTheme.colorScheme.onError
            borderColor = Color.Transparent
        }

        ValeVoipCallButtonType.ACCEPT -> {
            backgroundColor = Color(0xFF4CAF50) // Verde Vivo (Material Green 500)
            iconColor = Color.White
            borderColor = Color.Transparent
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = if (borderColor != Color.Transparent) 2.dp else 0.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        label?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ValeVoipCallButtonPreview() {
    ValeVoipTheme {
        Column {
            ValeVoipCallButton(
                icon = Icons.Default.Dialpad,
                label = "Teclado",
                buttonType = ValeVoipCallButtonType.DEFAULT,
                onClick = { }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Preview do Toggle Ativo
            ValeVoipCallButton(
                icon = Icons.Default.Dialpad,
                label = "Mutado (Ativo)",
                isActive = false,
                buttonType = ValeVoipCallButtonType.TOGGLE,
                onClick = { }
            )
        }
    }
}