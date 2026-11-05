package com.valevoip.feature.call

import androidx.lifecycle.SavedStateHandle
import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.AudioRouteType
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.usecase.AnswerCallUseCase
import com.valevoip.core.domain.usecase.GetCallStatusSyncUseCase
import com.valevoip.core.domain.usecase.GetCurrentCallNumberUseCase
import com.valevoip.core.domain.usecase.HangUpUseCase
import com.valevoip.core.domain.usecase.MakeCallUseCase
import com.valevoip.core.domain.usecase.ObserveActiveAudioRouteUseCase
import com.valevoip.core.domain.usecase.ObserveAudioRoutesUseCase
import com.valevoip.core.domain.usecase.ObserveCallStateUseCase
import com.valevoip.core.domain.usecase.ObserveMuteStateUseCase
import com.valevoip.core.domain.usecase.ObserveSpeakerStateUseCase
import com.valevoip.core.domain.usecase.SelectAudioRouteUseCase
import com.valevoip.core.domain.usecase.SendDtmfUseCase
import com.valevoip.core.domain.usecase.ToggleHoldUseCase
import com.valevoip.core.domain.usecase.ToggleMuteUseCase
import com.valevoip.core.domain.usecase.ToggleSpeakerUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
internal class CallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val makeCallUseCase: MakeCallUseCase = mockk()
    private val hangUpUseCase: HangUpUseCase = mockk()
    private val answerCallUseCase: AnswerCallUseCase = mockk()
    private val toggleMuteUseCase: ToggleMuteUseCase = mockk()
    private val toggleSpeakerUseCase: ToggleSpeakerUseCase = mockk()
    private val observeCallStateUseCase: ObserveCallStateUseCase = mockk()
    private val getCallStatusSyncUseCase: GetCallStatusSyncUseCase = mockk()
    private val getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase = mockk()
    private val observeMuteStateUseCase: ObserveMuteStateUseCase = mockk()
    private val observeSpeakerStateUseCase: ObserveSpeakerStateUseCase = mockk()
    private val observeAudioRoutesUseCase: ObserveAudioRoutesUseCase = mockk()
    private val observeActiveAudioRouteUseCase: ObserveActiveAudioRouteUseCase = mockk()
    private val selectAudioRouteUseCase: SelectAudioRouteUseCase = mockk()
    private val sendDtmfUseCase: SendDtmfUseCase = mockk()
    private val toggleHoldUseCase: ToggleHoldUseCase = mockk()

    private lateinit var callStatusFlow: MutableStateFlow<CallStatus>
    private lateinit var muteStateFlow: MutableStateFlow<Boolean>
    private lateinit var speakerStateFlow: MutableStateFlow<Boolean>
    private lateinit var audioRoutesFlow: MutableStateFlow<List<AudioRoute>>
    private lateinit var activeAudioRouteFlow: MutableStateFlow<AudioRoute?>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        callStatusFlow = MutableStateFlow(CallStatus.IDLE)
        muteStateFlow = MutableStateFlow(false)
        speakerStateFlow = MutableStateFlow(false)
        audioRoutesFlow = MutableStateFlow(emptyList())
        activeAudioRouteFlow = MutableStateFlow(null)

        every { observeCallStateUseCase() } returns callStatusFlow
        every { observeMuteStateUseCase() } returns muteStateFlow
        every { observeSpeakerStateUseCase() } returns speakerStateFlow
        every { observeAudioRoutesUseCase() } returns audioRoutesFlow
        every { observeActiveAudioRouteUseCase() } returns activeAudioRouteFlow
        every { getCallStatusSyncUseCase() } returns CallStatus.IDLE
        every { getCurrentCallNumberUseCase() } returns "Desconhecido"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `discar ramal 2525 chama exatamente 2525 sem desvio hardcoded`() = runTest {
        every { makeCallUseCase("2525") } returns Result.success(Unit)
        val viewModel = createViewModel(number = "2525")

        viewModel.onEvent(CallUiEvent.OnPermissionGranted)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("2525", viewModel.uiState.value.contactNumber)
        verify(exactly = 1) { makeCallUseCase("2525") }
    }

    @Test
    fun `discar ramal alfanumerico alexandreskt16 chama exatamente alexandreskt16`() = runTest {
        every { makeCallUseCase("alexandreskt16") } returns Result.success(Unit)
        val viewModel = createViewModel(number = "alexandreskt16")

        viewModel.onEvent(CallUiEvent.OnPermissionGranted)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("alexandreskt16", viewModel.uiState.value.contactNumber)
        verify(exactly = 1) { makeCallUseCase("alexandreskt16") }
    }

    @Test
    fun `discar ramal 5555 chama exatamente 5555 sem desvio hardcoded`() = runTest {
        every { makeCallUseCase("5555") } returns Result.success(Unit)
        val viewModel = createViewModel(number = "5555")

        viewModel.onEvent(CallUiEvent.OnPermissionGranted)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("5555", viewModel.uiState.value.contactNumber)
        verify(exactly = 1) { makeCallUseCase("5555") }
    }

    @Test
    fun `chamada recebida via deep link sem numero na rota hidrata contactNumber do SipClient`() = runTest {
        every { getCallStatusSyncUseCase() } returns CallStatus.INCOMING
        every { getCurrentCallNumberUseCase() } returns "3040"
        callStatusFlow.value = CallStatus.INCOMING

        val viewModel = createViewModel(number = null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("3040", viewModel.uiState.value.contactNumber)
        assertEquals(CallStatus.INCOMING, viewModel.uiState.value.callStatus)
    }

    @Test
    fun `chamada recebida atualiza Desconhecido para numero real quando disponivel no evento INCOMING`() = runTest {
        every { getCallStatusSyncUseCase() } returns CallStatus.IDLE
        every { getCurrentCallNumberUseCase() } returnsMany listOf("Desconhecido", "4050")

        val viewModel = createViewModel(number = null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Desconhecido", viewModel.uiState.value.contactNumber)

        callStatusFlow.value = CallStatus.INCOMING
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("4050", viewModel.uiState.value.contactNumber)
        assertEquals(CallStatus.INCOMING, viewModel.uiState.value.callStatus)
    }

    @Test
    fun `ignora CallStatus ENDED residual da chamada anterior enquanto aguarda permissao para nova discagem`() = runTest {
        callStatusFlow.value = CallStatus.ENDED
        every { getCallStatusSyncUseCase() } returns CallStatus.ENDED
        every { makeCallUseCase("1008") } returns Result.success(Unit)

        val viewModel = createViewModel(number = "1008")
        testDispatcher.scheduler.advanceUntilIdle()

        // Enquanto aguarda permissão, não propaga ENDED residual que fecharia a tela prematuramente
        assertEquals(CallStatus.IDLE, viewModel.uiState.value.callStatus)

        viewModel.onEvent(CallUiEvent.OnPermissionGranted)
        callStatusFlow.value = CallStatus.DIALING
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CallStatus.DIALING, viewModel.uiState.value.callStatus)

        // Quando a nova chamada de fato encerrar, propaga ENDED normalmente
        callStatusFlow.value = CallStatus.ENDED
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CallStatus.ENDED, viewModel.uiState.value.callStatus)
    }

    @Test
    fun `sincroniza reativamente isMuted e isSpeakerOn emitidos pelo SipClient`() = runTest {
        val viewModel = createViewModel(number = "1001")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isMuted)
        assertFalse(viewModel.uiState.value.isSpeakerOn)

        muteStateFlow.value = true
        speakerStateFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isMuted)
        assertTrue(viewModel.uiState.value.isSpeakerOn)
    }

    @Test
    fun `OnAudioRouteClick alterna viva-voz diretamente quando so existem EARPIECE e SPEAKER`() = runTest {
        val earpiece = AudioRoute("earpiece_1", "Telefone", AudioRouteType.EARPIECE)
        val speaker = AudioRoute("speaker_1", "Viva-voz", AudioRouteType.SPEAKER)
        audioRoutesFlow.value = listOf(earpiece, speaker)
        every { toggleSpeakerUseCase() } returns Result.success(true)

        val viewModel = createViewModel(number = "1001")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CallUiEvent.OnAudioRouteClick)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAudioRouteSheetVisible)
        assertTrue(viewModel.uiState.value.isSpeakerOn)
        verify(exactly = 1) { toggleSpeakerUseCase() }
    }

    @Test
    fun `OnAudioRouteClick abre seletor quando existe dispositivo Bluetooth e OnSelectAudioRoute delega escolha`() = runTest {
        val earpiece = AudioRoute("earpiece_1", "Telefone", AudioRouteType.EARPIECE)
        val btHeadset = AudioRoute("bt_1", "Galaxy Buds", AudioRouteType.BLUETOOTH)
        audioRoutesFlow.value = listOf(earpiece, btHeadset)
        activeAudioRouteFlow.value = btHeadset
        every { selectAudioRouteUseCase("earpiece_1") } returns Result.success(Unit)

        val viewModel = createViewModel(number = "1001")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CallUiEvent.OnAudioRouteClick)
        assertTrue(viewModel.uiState.value.isAudioRouteSheetVisible)

        viewModel.onEvent(CallUiEvent.OnSelectAudioRoute("earpiece_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAudioRouteSheetVisible)
        verify(exactly = 1) { selectAudioRouteUseCase("earpiece_1") }
    }

    @Test
    fun `OnShowKeypad e OnDtmfDigit enviam tons DTMF e acumulam historico na UI`() = runTest {
        every { sendDtmfUseCase('1') } returns Result.success(Unit)
        every { sendDtmfUseCase('#') } returns Result.success(Unit)

        val viewModel = createViewModel(number = "1001")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CallUiEvent.OnShowKeypad)
        assertTrue(viewModel.uiState.value.isKeypadVisible)

        viewModel.onEvent(CallUiEvent.OnDtmfDigit('1'))
        viewModel.onEvent(CallUiEvent.OnDtmfDigit('#'))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1#", viewModel.uiState.value.dtmfInput)
        verify(exactly = 1) { sendDtmfUseCase('1') }
        verify(exactly = 1) { sendDtmfUseCase('#') }
    }

    @Test
    fun `OnToggleHold delega ao caso de uso, mantem cronometro em PAUSED e CallStatus ENDED fecha sheets abertos`() = runTest {
        every { toggleHoldUseCase() } returns Result.success(true)

        val viewModel = createViewModel(number = "1001")
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(CallUiEvent.OnShowKeypad)
        viewModel.onEvent(CallUiEvent.OnToggleHold)
        callStatusFlow.value = CallStatus.PAUSED
        testDispatcher.scheduler.runCurrent()

        assertEquals(CallStatus.PAUSED, viewModel.uiState.value.callStatus)
        assertTrue(viewModel.uiState.value.isKeypadVisible)
        verify(exactly = 1) { toggleHoldUseCase() }

        testDispatcher.scheduler.advanceTimeBy(2_000)
        testDispatcher.scheduler.runCurrent()
        assertEquals("00:02", viewModel.uiState.value.formattedDuration)

        callStatusFlow.value = CallStatus.ENDED
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CallStatus.ENDED, viewModel.uiState.value.callStatus)
        assertFalse(viewModel.uiState.value.isKeypadVisible)
        assertFalse(viewModel.uiState.value.isAudioRouteSheetVisible)
    }

    private fun createViewModel(number: String?): CallViewModel {
        val savedStateHandle = if (number != null) {
            SavedStateHandle(mapOf("number" to number))
        } else {
            SavedStateHandle()
        }
        return CallViewModel(
            savedStateHandle = savedStateHandle,
            makeCallUseCase = makeCallUseCase,
            hangUpUseCase = hangUpUseCase,
            answerCallUseCase = answerCallUseCase,
            toggleMuteUseCase = toggleMuteUseCase,
            toggleSpeakerUseCase = toggleSpeakerUseCase,
            observeCallStateUseCase = observeCallStateUseCase,
            getCallStatusSyncUseCase = getCallStatusSyncUseCase,
            getCurrentCallNumberUseCase = getCurrentCallNumberUseCase,
            observeMuteStateUseCase = observeMuteStateUseCase,
            observeSpeakerStateUseCase = observeSpeakerStateUseCase,
            observeAudioRoutesUseCase = observeAudioRoutesUseCase,
            observeActiveAudioRouteUseCase = observeActiveAudioRouteUseCase,
            selectAudioRouteUseCase = selectAudioRouteUseCase,
            sendDtmfUseCase = sendDtmfUseCase,
            toggleHoldUseCase = toggleHoldUseCase
        )
    }
}
