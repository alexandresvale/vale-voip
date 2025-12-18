package com.example.valevoip.presentation.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.valevoip.R
import com.example.valevoip.presentation.ui.theme.AppTypography
import com.example.valevoip.presentation.ui.theme.ValeVoipTheme
import com.example.valevoip.presentation.ui.theme.primaryLight
import com.example.valevoip.presentation.ui.util.SystemBarsController

@Composable
fun OnboardingLayout(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit
) {
    SystemBarsController(useDarkIcons = !isSystemInDarkTheme())

    OnboardingContent(
        uiState = uiState,
        onEvent = onEvent
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit
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
                painter = painterResource(R.drawable.logo_vale_voip),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                alignment = Alignment.TopCenter
            )

            Text(
                text = stringResource(R.string.vale_voip_onboarding_welcome),
                style = AppTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.vale_voip_onboarding_subtitle),
                style = AppTypography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isUserError = uiState.userNameError != null
            OutlinedTextField(
                value = uiState.userName,
                onValueChange = { onEvent(OnboardingUiEvent.OnUsernameChange(it)) },
                enabled = !isLoading,
                label = { Text(text = stringResource(R.string.vale_voip_onboarding_username_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = isUserError,
                supportingText = {
                    if (isUserError) {
                        Text(
                            text = uiState.userNameError,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(stringResource(R.string.vale_voip_onboarding_username_aux))
                    }
                },
                trailingIcon = {
                    if (isUserError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Erro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(focusDirection = FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isPassError = uiState.passwordError != null
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onEvent(OnboardingUiEvent.OnPasswordChange(it)) },
                enabled = !isLoading,
                label = { Text(text = stringResource(R.string.vale_voip_onboarding_password_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = isPassError,
                supportingText = if (isPassError) {
                    { Text(text = uiState.passwordError, color = MaterialTheme.colorScheme.error) }
                } else null,

                trailingIcon = {
                    if (isPassError) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isDomainError = uiState.domainError != null
            OutlinedTextField(
                value = uiState.domain,
                onValueChange = { onEvent(OnboardingUiEvent.OnDomainChange(it)) },
                enabled = !isLoading,
                label = { Text(text = stringResource(R.string.vale_voip_onboarding_port_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = isDomainError,
                supportingText = if (isDomainError) {
                    { Text(text = uiState.domainError, color = MaterialTheme.colorScheme.error) }
                } else null,

                trailingIcon = {
                    if (isDomainError) Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryLight,
                    focusedLabelColor = primaryLight,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onEvent(OnboardingUiEvent.OnRegisterClick)
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
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = primaryLight),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    focusManager.clearFocus()
                    onEvent(OnboardingUiEvent.OnRegisterClick)
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.vale_voip_onboarding_start_button),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, apiLevel = 33)
@Composable
fun OnboardingLayoutLightPreview() {
    ValeVoipTheme(darkTheme = false) {
        OnboardingLayout(
            uiState = OnboardingUiState(isLoading = false),
            onEvent = {}
        )
    }
}
