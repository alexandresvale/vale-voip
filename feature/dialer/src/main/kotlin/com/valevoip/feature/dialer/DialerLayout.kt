package com.valevoip.feature.dialer

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.DialerButton
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.feature.dialer.model.KeypadUtil

@Composable
internal fun DialerLayout(
    uiState: DialerUiState,
    onEvent: (DialerUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Área de Input (Alfanumérico SIP ou Numérico T9)
        Box(
            modifier = Modifier
                .weight(if (uiState.isAlphaNumericMode) 0.55f else 0.26f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isAlphaNumericMode) {
                AlphaNumericDialerInput(
                    number = uiState.number,
                    onEvent = onEvent
                )
            } else {
                NumericDialerInput(
                    number = uiState.number,
                    onEvent = onEvent
                )
            }
        }

        // 2. Área do Teclado Numérico e Barra Inferior de Ação
        Column(
            modifier = Modifier
                .weight(if (uiState.isAlphaNumericMode) 0.45f else 0.74f)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = !uiState.isAlphaNumericMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = if (!uiState.isAlphaNumericMode) Modifier.weight(1f) else Modifier
            ) {
                DialerKeypad(
                    modifier = Modifier.fillMaxSize(),
                    onDigitClick = { digit -> onEvent(DialerUiEvent.OnDigitClick(digit)) },
                    onLongClickZero = { onEvent(DialerUiEvent.OnLongClickZero) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    InputModeToggleButton(
                        isAlphaNumericMode = uiState.isAlphaNumericMode,
                        onClick = { onEvent(DialerUiEvent.OnToggleInputMode) }
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CallButton(
                        onClick = { onEvent(DialerUiEvent.OnCallClick) }
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ClearInputButton(
                        number = uiState.number,
                        isAlphaNumericMode = uiState.isAlphaNumericMode,
                        onClick = { onEvent(DialerUiEvent.OnClearAll) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlphaNumericDialerInput(
    number: String,
    onEvent: (DialerUiEvent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = number,
                selection = TextRange(number.length)
            )
        )
    }

    if (textFieldValue.text != number) {
        textFieldValue = TextFieldValue(
            text = number,
            selection = TextRange(number.length)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val textStyle = when {
        number.length > 22 -> MaterialTheme.typography.titleMedium
        number.length > 14 -> MaterialTheme.typography.headlineSmall
        else -> MaterialTheme.typography.headlineMedium
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEvent(DialerUiEvent.OnToggleInputMode) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Usar teclado numérico",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { updated ->
                            textFieldValue = updated
                            if (updated.text != number) {
                                onEvent(DialerUiEvent.OnTextChange(updated.text))
                            }
                        },
                        singleLine = true,
                        textStyle = textStyle.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = { onEvent(DialerUiEvent.OnCallClick) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (number.isEmpty()) {
                                    Text(
                                        text = "Ramal ou usuário SIP",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                DialerInputTrailingAction(
                    number = number,
                    onEvent = onEvent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ex.: 1002, valevoip, alexandreskt16 ou usuario@dominio",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NumericDialerInput(
    number: String,
    onEvent: (DialerUiEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    val textStyle = when {
        number.length > 22 -> MaterialTheme.typography.titleMedium
        number.length > 14 -> MaterialTheme.typography.headlineSmall
        else -> MaterialTheme.typography.displaySmall
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onEvent(DialerUiEvent.OnToggleInputMode) }
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Digitar letras ou usuário SIP",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onEvent(DialerUiEvent.OnActivateAlphaNumericMode) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (number.isEmpty()) {
                Text(
                    text = "Ramal ou usuário SIP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = number,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        DialerInputTrailingAction(
            number = number,
            onEvent = onEvent
        )
    }
}

@Composable
private fun DialerInputTrailingAction(
    number: String,
    onEvent: (DialerUiEvent) -> Unit
) {
    val context = LocalContext.current
    if (number.isNotEmpty()) {
        BackspaceButton(
            onClick = { onEvent(DialerUiEvent.OnBackspace) },
            onLongClick = { onEvent(DialerUiEvent.OnClearAll) }
        )
    } else {
        IconButton(
            onClick = {
                val pasted = readClipboardText(context)
                if (!pasted.isNullOrBlank()) {
                    onEvent(DialerUiEvent.OnTextChange(pasted.trim()))
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "Colar ramal ou endereço SIP",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun InputModeToggleButton(
    isAlphaNumericMode: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isAlphaNumericMode) Icons.Default.Dialpad else Icons.Default.Keyboard,
                contentDescription = if (isAlphaNumericMode) {
                    "Alternar para teclado numérico 123"
                } else {
                    "Alternar para teclado de letras ABC"
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isAlphaNumericMode) "123" else "ABC",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClearInputButton(
    number: String,
    isAlphaNumericMode: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (number.isNotEmpty() && isAlphaNumericMode) {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpar campo"
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Limpar campo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BackspaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (onLongClick != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Apagar (segure para limpar tudo)",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
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

private fun readClipboardText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = clipboard?.primaryClip ?: return null
    if (clip.itemCount <= 0) return null
    return clip.getItemAt(0)?.coerceToText(context)?.toString()
}

@Preview(name = "Modo Numérico 123")
@Composable
internal fun DialerLayoutPreview() {
    ValeVoipTheme(darkTheme = false) {
        DialerLayout(
            uiState = DialerUiState(number = "2525", isAlphaNumericMode = false),
            onEvent = {}
        )
    }
}

@Preview(name = "Modo Alfanumérico SIP ABC")
@Composable
internal fun DialerLayoutAlphaPreview() {
    ValeVoipTheme(darkTheme = false) {
        DialerLayout(
            uiState = DialerUiState(number = "alexandreskt16", isAlphaNumericMode = true),
            onEvent = {}
        )
    }
}
