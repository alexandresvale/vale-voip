package com.valevoip.feature.login

import com.valevoip.core.domain.usecase.RegisterAccountUseCase
import com.valevoip.core.domain.usecase.RegisterError
import com.valevoip.core.domain.usecase.RegisterResult
import io.mockk.coEvery
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
    fun `Quando o clique em registrar falhar por campo vazio, o estado deve exibir erro`() = runTest {
        // Given
        val messageError = "O domínio é obrigatório"
        coEvery {
            registerAccountUseCase.invoke(
                any(),
                any(),
                any()
            )
        } returns RegisterResult.Error(RegisterError.EmptyDomain)

        // When
        viewModel.onEvent(LoginUiEvent.OnRegisterClick)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val estadoAtual = viewModel.uiState.value
        assertEquals(messageError, estadoAtual.domainError)
        assertEquals(false, estadoAtual.isLoading)
        assertEquals(true, estadoAtual.isConnectionError)
    }
}