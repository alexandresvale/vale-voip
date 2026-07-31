package com.valevoip.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.component.ValeVoipPrimaryButton
import com.valevoip.core.designsystem.component.ValeVoipTextField
import com.valevoip.core.designsystem.theme.AppTypography
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.designsystem.R as DesignR

@Composable
internal fun LoginLayout(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isLoading = uiState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(DesignR.drawable.logo_vale_voip),
                contentDescription = stringResource(DesignR.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                alignment = Alignment.TopCenter
            )

            Text(
                text = stringResource(R.string.vale_voip_login_welcome),
                style = AppTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.vale_voip_login_subtitle),
                style = AppTypography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            ValeVoipTextField(
                value = uiState.userName,
                onValueChange = { onEvent(LoginUiEvent.OnUsernameChange(it)) },
                label = stringResource(R.string.vale_voip_login_username_hint),
                enabled = !isLoading,
                isError = uiState.userNameError != null,
                errorMessage = uiState.userNameError,
                supportingTextString = stringResource(R.string.vale_voip_login_username_aux),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(focusDirection = FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ValeVoipTextField(
                value = uiState.password,
                onValueChange = { onEvent(LoginUiEvent.OnPasswordChange(it)) },
                label = stringResource(R.string.vale_voip_login_password_hint),
                enabled = !isLoading,
                isPassword = true,
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ValeVoipTextField(
                value = uiState.domain,
                onValueChange = { onEvent(LoginUiEvent.OnDomainChange(it)) },
                label = stringResource(R.string.vale_voip_login_port_hint),
                enabled = !isLoading,
                isError = uiState.domainError != null,
                errorMessage = uiState.domainError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onEvent(LoginUiEvent.OnRegisterClick)
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        uiState.statusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.isConnectionError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ValeVoipPrimaryButton(
                text = stringResource(R.string.vale_voip_login_start_button),
                isLoading = isLoading,
                onClick = {
                    focusManager.clearFocus()
                    onEvent(LoginUiEvent.OnRegisterClick)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginLayoutLightPreview() {
    ValeVoipTheme(darkTheme = false) {
        LoginLayout(
            uiState = LoginUiState(isLoading = false),
            onEvent = {}
        )
    }
}
