package com.valevoip.feature.dialer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DialerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DialerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DialerViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `digitar ramal numerico acumula digitos, suporta backspace e limpa tudo`() {
        viewModel.onEvent(DialerUiEvent.OnDigitClick("2"))
        viewModel.onEvent(DialerUiEvent.OnDigitClick("5"))
        viewModel.onEvent(DialerUiEvent.OnDigitClick("2"))
        viewModel.onEvent(DialerUiEvent.OnDigitClick("5"))

        assertEquals("2525", viewModel.uiState.value.number)

        viewModel.onEvent(DialerUiEvent.OnBackspace)
        assertEquals("252", viewModel.uiState.value.number)

        viewModel.onEvent(DialerUiEvent.OnLongClickZero)
        assertEquals("252+", viewModel.uiState.value.number)

        viewModel.onEvent(DialerUiEvent.OnClearAll)
        assertEquals("", viewModel.uiState.value.number)
    }

    @Test
    fun `alterna entre modo numerico T9 e modo alfanumerico QWERTY estilo Linphone`() {
        assertFalse(viewModel.uiState.value.isAlphaNumericMode)

        viewModel.onEvent(DialerUiEvent.OnToggleInputMode)
        assertTrue(viewModel.uiState.value.isAlphaNumericMode)

        viewModel.onEvent(DialerUiEvent.OnActivateAlphaNumericMode)
        assertTrue(viewModel.uiState.value.isAlphaNumericMode)

        viewModel.onEvent(DialerUiEvent.OnToggleInputMode)
        assertFalse(viewModel.uiState.value.isAlphaNumericMode)
    }

    @Test
    fun `permite digitar e discar ramal alfanumerico como valevoip e alexandreskt16 removendo espacos e quebras de linha`() =
        runTest {
            viewModel.onEvent(DialerUiEvent.OnActivateAlphaNumericMode)
            viewModel.onEvent(DialerUiEvent.OnTextChange("  alexandreskt16\n\r  "))

            assertEquals("  alexandreskt16  ", viewModel.uiState.value.number)

            viewModel.onEvent(DialerUiEvent.OnCallClick)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = viewModel.effects.first()
            assertEquals(DialerEffect.NavigateToCall("alexandreskt16"), effect)
        }

    @Test
    fun `discar ramal valevoip emite efeito NavigateToCall com destino valevoip`() = runTest {
        viewModel.onEvent(DialerUiEvent.OnTextChange("valevoip"))
        viewModel.onEvent(DialerUiEvent.OnCallClick)
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = viewModel.effects.first()
        assertEquals(DialerEffect.NavigateToCall("valevoip"), effect)
    }

    @Test
    fun `clicar em ligar com campo vazio ou apenas espacos emite ShowError`() = runTest {
        viewModel.onEvent(DialerUiEvent.OnTextChange("   "))
        viewModel.onEvent(DialerUiEvent.OnCallClick)
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = viewModel.effects.first()
        assertTrue(effect is DialerEffect.ShowError)
    }
}
