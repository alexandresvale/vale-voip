package com.valevoip.feature.login

import com.valevoip.core.analytics.AnalyticsTracker
import com.valevoip.core.domain.usecase.RegisterAccountUseCase
import com.valevoip.core.domain.usecase.RegisterError
import com.valevoip.core.domain.usecase.RegisterResult
import com.valevoip.core.domain.model.SipTransport
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(registerAccountUseCase, analyticsTracker)
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
    fun `TCP e a porta 5060 sao os valores padrao da tela de login`() {
        assertEquals(SipTransport.TCP, viewModel.uiState.value.transport)
        assertEquals("5060", viewModel.uiState.value.port)
    }

    @Test
    fun `Quando o evento OnPortChange for disparado, deve aceitar apenas digitos`() {
        viewModel.onEvent(LoginUiEvent.OnPortChange("5061abc"))

        assertEquals("5061", viewModel.uiState.value.port)
        assertEquals(null, viewModel.uiState.value.portError)
    }

    @Test
    fun `Quando o evento OnTransportChange for disparado, o estado da UI deve ser atualizado`() {
        viewModel.onEvent(LoginUiEvent.OnTransportChange(SipTransport.UDP))
        assertEquals(SipTransport.UDP, viewModel.uiState.value.transport)

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
            registerAccountUseCase.invoke(any(), any(), any(), "5060", SipTransport.TLS)
        }
        verify {
            analyticsTracker.logEvent(match { it.eventName == "sip_register_failure" })
        }
    }
}