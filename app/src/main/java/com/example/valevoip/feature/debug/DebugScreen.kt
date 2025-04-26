package com.example.valevoip.feature.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.valevoip.ui.componet.AlertDialogCompose
import com.example.valevoip.ui.componet.CircularProgress
import com.example.valevoip.ui.theme.ValeVoipTheme

@Composable
fun DebugScreen(
    debugViewModel: DebugViewModel = hiltViewModel()
) {

    val state by debugViewModel.uiState.collectAsState()

    DebugContent(
        state = state,
        action = { action -> debugViewModel.onAction(action) },
        name = "Android Debug",
        version = "Versão de debug",
    )
}

@Composable
fun DebugContent(
    state: DebugState = DebugState(),
    action: ((DebugAction) -> Unit)? = null,
    name: String,
    version: String,
    openAlertDialog: Boolean = false,
    onLogin: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDelete: () -> Unit = {},
    onAccept: () -> Unit = {},

    ) {
    val openAlertDialogState = remember { mutableStateOf(openAlertDialog) }
    val versionState = remember { mutableStateOf(version) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        CircularProgress(isLoading = state.isLoading)
        Text(
            text = "Hello $name!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Text(
            text = "Status da Conexão: ${state.message ?: ""}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Button(
            onClick = { action?.invoke(DebugAction.RegisterUser) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        )
        {
            Text(text = "Registrar")
        }
        Button(
            onClick = {
                action?.invoke(DebugAction.Unregister)
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        )
        {
            Text(text = "Deslogar")
        }
        Button(
            onClick = { action?.invoke(DebugAction.FetchUser) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        )
        {
            Text(text = "Obter Usuário")
        }
        Button(
            onClick = { action?.invoke(DebugAction.IncomingCall) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        )
        {
            Text(text = "Aceitar ligação")
        }
        Button(
            onClick = { action?.invoke(DebugAction.RegisterUser) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        )
        {
            Text(text = "Registar Conta")
        }
    }

    if (openAlertDialogState.value) {
        AlertDialogCompose(
            icon = Icons.Sharp.Warning,
            dialogTitle = "Aviso",
            dialogMessage = "É necessário ativar as permissões para o aplicativo funcionar corretamente",
            onDismissRequest = { openAlertDialogState.value = false },
            onConfirmationClick = { openAlertDialogState.value = false },
            onDismissClick = { openAlertDialogState.value = false }
        )
    }


}


@Preview(apiLevel = 33, showSystemUi = true)
@Composable
fun DebugContentPreview(
    @PreviewParameter(DebugStateParameterProvider::class) debugState: DebugState
) {
    ValeVoipTheme {
        DebugContent(
            state = debugState,
            name = "Android",
            version = "Versão"
        )
    }
}

class DebugStateParameterProvider : PreviewParameterProvider<DebugState> {
    override val values = sequenceOf(
        DebugState(isLoading = false, registrationStatus = "Carregando..."),
        /*DebugState(isLoading = false, registrationStatus = "Falha"),
        DebugState(isLoading = false, isDialogOpen = true)*/
    )
}