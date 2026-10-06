package com.valevoip.feature.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import org.junit.Rule
import org.junit.Test

internal class LoginLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifica_se_os_campos_sao_preenchidos_e_clique_aciona_evento() {
        // Given
        val uiState = LoginUiState(
            isLoading = false,
            /*userName = "alexandre",
            password = "123456",
            domain = "sip.valevoip.com"*/
        )
        var cliqueRegistrarAcionado = false

        // When
        composeTestRule.setContent {
            ValeVoipTheme {
                LoginLayout(
                    snackBarHostState = SnackbarHostState(),
                    uiState = uiState,
                    onEvent = { event ->
                        if (event is LoginUiEvent.OnRegisterClick) {
                            cliqueRegistrarAcionado = true
                        }
                    }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Nome de usuário").performTextInput("alexandre")
        composeTestRule.onNodeWithText("Senha").performTextInput("123456")
        composeTestRule.onNodeWithText("Domínio").performTextInput("sip.valevoip.com")
        composeTestRule.onNodeWithText("Iniciar").performClick()
        assert(cliqueRegistrarAcionado) { "O evento de clique não foi repassado para o Layout." }
    }

    @Test
    fun quando_uiState_contem_erro_a_mensagem_de_erro_deve_aparecer() {
        composeTestRule.setContent {
            ValeVoipTheme {
                LoginLayout(
                    snackBarHostState = SnackbarHostState(),
                    uiState = LoginUiState(
                        isLoading = false,
                        isConnectionError = true,
                        statusMessage = "Falha ao registrar no SIP"
                    ),
                    onEvent = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Falha ao registrar no SIP").assertExists()
    }
}