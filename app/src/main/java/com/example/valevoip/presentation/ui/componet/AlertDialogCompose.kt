package com.example.valevoip.presentation.ui.componet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AlertDialogCompose(
    dialogTitle: String,
    dialogMessage: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
    onConfirmationClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        icon = { Icon(imageVector = icon, contentDescription = "") },
        title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold) },
        text = { Text(text = dialogMessage) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmationClick() }) {
                Text(text = "Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissClick()}) {
                Text(text = "Cancelar")
            }
        }
    )
}

@Composable
@Preview
fun AlertDialogComposePreview() {
    AlertDialogCompose(
        icon = Icons.Sharp.Warning,
        dialogTitle = "Aviso",
        dialogMessage = "É necessário ativar as permissões para o aplicativo funcionar corretamente",
        onDismissRequest = {},
        onConfirmationClick = {},
        onDismissClick = {}
    )
}