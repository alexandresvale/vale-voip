package com.example.valevoip.presentation.ui.componet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme

@OptIn(ExperimentalFoundationApi::class) // Necessário para combinedClickable
@Composable
fun DialerButton(
    symbol: String,
    modifier: Modifier = Modifier,
    subText: String = "",
    color: Color = MaterialTheme.colorScheme.onSurface,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    if (onLongClick != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
                color = color
            )

            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = color.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview
@Composable
fun DialerButtonPreview() {
    ValeVoipTheme {
        DialerButton(
            symbol = "2",
            subText = "ABC",
            onLongClick = {},
            onClick = {}
        )
    }
}