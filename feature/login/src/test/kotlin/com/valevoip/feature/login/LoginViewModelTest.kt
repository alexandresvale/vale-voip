package com.valevoip.feature.login

import com.valevoip.core.domain.usecase.RegisterAccountUseCase
import com.valevoip.core.domain.usecase.RegisterError
import com.valevoip.core.domain.usecase.RegisterResult
import com.valevoip.core.domain.model.SipTransport
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val registerAccountUseCase: RegisterAccountUseCase = mockk()
    private lateinit var viewModel: LoginViewModel


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(registerAccountUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Quando o evento OnUsernameChange for disparado, o estado da UI deve ser atualizado`() {
        // Given
        val username = "admin"

        // When
        viewModel.onEvent(LoginUiEvent.OnUsernameChange(username))

        // Then
        val estadoAtual = viewModel.uiState.value
        assertEquals(username, estadoAtual.userName)
        assertEquals(null, estadoAtual.userNameError)
    }

    @Test
    fun `TCP e o transporte padrao da tela de login`() {
        assertEquals(SipTransport.TCP, viewModel.uiState.value.transport)
    }

    @Test
    fun `Quando o evento OnTransportChange for disparado, o estado da UI deve ser atualizado`() {
        viewModel.onEvent(LoginUiEvent.OnTransportChange(SipTransport.TLS))

        assertEquals(SipTransport.TLS, viewModel.uiState.value.transport)
    }

    @Test
    fun `Quando o clique em registrar falhar por campo vazio, o estado deve exibir erro`() = runTest {
        // Given
        val messageError = "O domínio é obrigatório"
        coEvery {
            registerAccountUseCase.invoke(
                any(),
                any(),
                any(),
                any()
            )
        } returns RegisterResult.Error(RegisterError.EmptyDomain)

        // When
        viewModel.onEvent(LoginUiEvent.OnTransportChange(SipTransport.TLS))
        viewModel.onEvent(LoginUiEvent.OnRegisterClick)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val estadoAtual = viewModel.uiState.value
        assertEquals(messageError, estadoAtual.domainError)
        assertEquals(false, estadoAtual.isLoading)
        assertEquals(true, estadoAtual.isConnectionError)
        coVerify {
            registerAccountUseCase.invoke(any(), any(), any(), SipTransport.TLS)
        }
    }
}