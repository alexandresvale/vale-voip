package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.BatteryOptimizationProvider
import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.AudioRoute
import com.valevoip.core.domain.model.AudioRouteType
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.repository.AccountRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelephonyUseCasesTest {

    private lateinit var fakeSipClient: FakeSipClient
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var fakeBatteryOptimizationProvider: FakeBatteryOptimizationProvider

    private lateinit var answerCallUseCase: AnswerCallUseCase
    private lateinit var hangUpUseCase: HangUpUseCase
    private lateinit var toggleMuteUseCase: ToggleMuteUseCase
    private lateinit var toggleSpeakerUseCase: ToggleSpeakerUseCase
    private lateinit var getCallStatusSyncUseCase: GetCallStatusSyncUseCase
    private lateinit var getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase
    private lateinit var observeCallStateUseCase: ObserveCallStateUseCase
    private lateinit var observeMuteStateUseCase: ObserveMuteStateUseCase
    private lateinit var observeSpeakerStateUseCase: ObserveSpeakerStateUseCase
    private lateinit var observeAudioRoutesUseCase: ObserveAudioRoutesUseCase
    private lateinit var observeActiveAudioRouteUseCase: ObserveActiveAudioRouteUseCase
    private lateinit var selectAudioRouteUseCase: SelectAudioRouteUseCase
    private lateinit var sendDtmfUseCase: SendDtmfUseCase
    private lateinit var toggleHoldUseCase: ToggleHoldUseCase
    private lateinit var isIgnoringBatteryOptimizationsUseCase: IsIgnoringBatteryOptimizationsUseCase
    private lateinit var getAccountUseCase: GetAccountUseCase
    private lateinit var unregisterAccountUseCase: UnregisterAccountUseCase
    private lateinit var observeRegistrationStateUseCase: ObserveRegistrationStateUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        fakeSipClient = FakeSipClient()
        fakeAccountRepository = FakeAccountRepository()
        fakeBatteryOptimizationProvider = FakeBatteryOptimizationProvider()

        answerCallUseCase = AnswerCallUseCase(fakeSipClient)
        hangUpUseCase = HangUpUseCase(fakeSipClient)
        toggleMuteUseCase = ToggleMuteUseCase(fakeSipClient)
        toggleSpeakerUseCase = ToggleSpeakerUseCase(fakeSipClient)
        getCallStatusSyncUseCase = GetCallStatusSyncUseCase(fakeSipClient)
        getCurrentCallNumberUseCase = GetCurrentCallNumberUseCase(fakeSipClient)
        observeCallStateUseCase = ObserveCallStateUseCase(fakeSipClient)
        observeMuteStateUseCase = ObserveMuteStateUseCase(fakeSipClient)
        observeSpeakerStateUseCase = ObserveSpeakerStateUseCase(fakeSipClient)
        observeAudioRoutesUseCase = ObserveAudioRoutesUseCase(fakeSipClient)
        observeActiveAudioRouteUseCase = ObserveActiveAudioRouteUseCase(fakeSipClient)
        selectAudioRouteUseCase = SelectAudioRouteUseCase(fakeSipClient)
        sendDtmfUseCase = SendDtmfUseCase(fakeSipClient)
        toggleHoldUseCase = ToggleHoldUseCase(fakeSipClient)
        isIgnoringBatteryOptimizationsUseCase = IsIgnoringBatteryOptimizationsUseCase(fakeBatteryOptimizationProvider)
        getAccountUseCase = GetAccountUseCase(fakeAccountRepository)
        unregisterAccountUseCase = UnregisterAccountUseCase(fakeSipClient, fakeAccountRepository, testDispatcher)
        observeRegistrationStateUseCase = ObserveRegistrationStateUseCase(fakeSipClient)
    }

    @Test
    fun `answerCall delegates to sipClient acceptCall`() {
        val result = answerCallUseCase()
        assertTrue(result.isSuccess)
        assertTrue(fakeSipClient.acceptedCall)
    }

    @Test
    fun `hangUp delegates to sipClient hangUp`() {
        val result = hangUpUseCase()
        assertTrue(result.isSuccess)
        assertTrue(fakeSipClient.hungUp)
    }

    @Test
    fun `toggleMute delegates to sipClient toggleMute`() {
        val result = toggleMuteUseCase()
        assertTrue(result.isSuccess)
        assertTrue(fakeSipClient.mutedToggled)
    }

    @Test
    fun `toggleSpeaker delegates to sipClient toggleSpeaker`() {
        val result = toggleSpeakerUseCase()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        assertTrue(fakeSipClient.speakerToggled)
    }

    @Test
    fun `toggleHold delegates to sipClient toggleHold`() {
        val result = toggleHoldUseCase()
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        assertTrue(fakeSipClient.holdToggled)
    }

    @Test
    fun `sendDtmf accepts valid RFC 4733 digits and normalizes lowercase a-d`() {
        assertTrue(sendDtmfUseCase('5').isSuccess)
        assertEquals('5', fakeSipClient.lastDtmfDigit)

        assertTrue(sendDtmfUseCase('#').isSuccess)
        assertEquals('#', fakeSipClient.lastDtmfDigit)

        assertTrue(sendDtmfUseCase('b').isSuccess)
        assertEquals('B', fakeSipClient.lastDtmfDigit)
    }

    @Test
    fun `sendDtmf rejects invalid character with IllegalArgumentException`() {
        val result = sendDtmfUseCase('Z')
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `selectAudioRoute delegates valid routeId and rejects blank routeId`() {
        val validResult = selectAudioRouteUseCase("bt_headset_1")
        assertTrue(validResult.isSuccess)
        assertEquals("bt_headset_1", fakeSipClient.lastSelectedRouteId)

        val blankResult = selectAudioRouteUseCase("   ")
        assertTrue(blankResult.isFailure)
        assertTrue(blankResult.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `observeAudioRoutes and observeActiveAudioRoute emit flows from sipClient`() = runTest {
        val btRoute = AudioRoute("bt_1", "Galaxy Buds", AudioRouteType.BLUETOOTH)
        fakeSipClient.availableAudioRoutesFlow.value = listOf(btRoute)
        fakeSipClient.activeAudioRouteFlow.value = btRoute

        assertEquals(listOf(btRoute), observeAudioRoutesUseCase().first())
        assertEquals(btRoute, observeActiveAudioRouteUseCase().first())
    }

    @Test
    fun `getCallStatusSync delegates to sipClient getSynchronousCallStatus`() {
        fakeSipClient.currentStatus = CallStatus.ACTIVE
        assertEquals(CallStatus.ACTIVE, getCallStatusSyncUseCase())
    }

    @Test
    fun `getCurrentCallNumber delegates to sipClient getCurrentCallNumber`() {
        fakeSipClient.currentNumber = "1002"
        assertEquals("1002", getCurrentCallNumberUseCase())
    }

    @Test
    fun `observeCallState emits status flow from sipClient`() = runTest {
        fakeSipClient.callStatusFlow.value = CallStatus.RINGING
        assertEquals(CallStatus.RINGING, observeCallStateUseCase().first())
    }

    @Test
    fun `observeMuteState emits mute state flow from sipClient`() = runTest {
        fakeSipClient.isMutedFlow.value = true
        assertTrue(observeMuteStateUseCase().first())
    }

    @Test
    fun `observeSpeakerState emits speaker state flow from sipClient`() = runTest {
        fakeSipClient.isSpeakerEnabledFlow.value = true
        assertTrue(observeSpeakerStateUseCase().first())
    }

    @Test
    fun `isIgnoringBatteryOptimizations delegates to provider`() {
        fakeBatteryOptimizationProvider.isIgnoringBatteryOptimizations = false
        assertFalse(isIgnoringBatteryOptimizationsUseCase())

        fakeBatteryOptimizationProvider.isIgnoringBatteryOptimizations = true
        assertTrue(isIgnoringBatteryOptimizationsUseCase())
    }

    @Test
    fun `getAccount returns saved account from repository`() = runTest {
        val account = SipAccount("1001", "pass", "domain.com")
        fakeAccountRepository.insertAccount(account)

        assertEquals(account, getAccountUseCase())
    }

    @Test
    fun `unregisterAccount clears account in repository and succeeds`() = runTest {
        val account = SipAccount("1001", "pass", "domain.com")
        fakeAccountRepository.insertAccount(account)
        fakeSipClient.unregisterEmission = flowOf(SipRegistrationState.None)

        val result = unregisterAccountUseCase()

        assertTrue(result.isSuccess)
        assertEquals(null, fakeAccountRepository.account)
    }

    @Test
    fun `unregisterAccount clears account even if sipClient throws`() = runTest {
        val account = SipAccount("1001", "pass", "domain.com")
        fakeAccountRepository.insertAccount(account)
        fakeSipClient.unregisterShouldThrow = true

        val result = unregisterAccountUseCase()

        assertTrue(result.isSuccess)
        assertEquals(null, fakeAccountRepository.account)
    }

    @Test
    fun `observeRegistrationState emits state flow from sipClient`() = runTest {
        fakeSipClient.registrationStateFlow.value = SipRegistrationState.Ok
        assertEquals(SipRegistrationState.Ok, observeRegistrationStateUseCase().first())
    }

    private class FakeSipClient : SipClient {
        var acceptedCall = false
        var hungUp = false
        var mutedToggled = false
        var speakerToggled = false
        var holdToggled = false
        var lastDtmfDigit: Char? = null
        var lastSelectedRouteId: String? = null
        var currentStatus = CallStatus.IDLE
        var currentNumber: String? = null
        val callStatusFlow = MutableStateFlow(CallStatus.IDLE)
        val registrationStateFlow = MutableStateFlow<SipRegistrationState>(SipRegistrationState.None)
        val isMutedFlow = MutableStateFlow(false)
        val isSpeakerEnabledFlow = MutableStateFlow(false)
        val availableAudioRoutesFlow = MutableStateFlow<List<AudioRoute>>(emptyList())
        val activeAudioRouteFlow = MutableStateFlow<AudioRoute?>(null)
        var unregisterEmission: Flow<SipRegistrationState> = emptyFlow()
        var unregisterShouldThrow = false

        override val registrationState: StateFlow<SipRegistrationState> = registrationStateFlow
        override val isMuted: StateFlow<Boolean> = isMutedFlow
        override val isSpeakerEnabled: StateFlow<Boolean> = isSpeakerEnabledFlow
        override val availableAudioRoutes: StateFlow<List<AudioRoute>> = availableAudioRoutesFlow
        override val activeAudioRoute: StateFlow<AudioRoute?> = activeAudioRouteFlow
        override suspend fun register(account: SipAccount): Flow<SipRegistrationState> = emptyFlow()
        override suspend fun unregister(): Flow<SipRegistrationState> {
            if (unregisterShouldThrow) throw IllegalStateException("Network Error")
            return unregisterEmission
        }
        override fun makeCall(number: String): Result<Unit> = Result.success(Unit)
        override fun hangUp(): Result<Unit> { hungUp = true; return Result.success(Unit) }
        override fun toggleMute(): Result<Unit> { mutedToggled = true; return Result.success(Unit) }
        override fun toggleSpeaker(): Result<Boolean> { speakerToggled = true; return Result.success(true) }
        override fun selectAudioRoute(routeId: String): Result<Unit> {
            lastSelectedRouteId = routeId
            return Result.success(Unit)
        }
        override fun sendDtmf(digit: Char): Result<Unit> {
            lastDtmfDigit = digit
            return Result.success(Unit)
        }
        override fun toggleHold(): Result<Boolean> {
            holdToggled = true
            return Result.success(true)
        }
        override fun muteCall(callId: String): Result<Unit> = Result.success(Unit)
        override fun acceptCall(): Result<Unit> { acceptedCall = true; return Result.success(Unit) }
        override fun getCallStatusFlow(): Flow<CallStatus> = callStatusFlow
        override fun getCurrentCallNumber(): String? = currentNumber
        override fun getSynchronousCallStatus(): CallStatus = currentStatus
        override fun getLinphoneVersion(): String = "5.0.0"
    }

    private class FakeAccountRepository : AccountRepository {
        var account: SipAccount? = null
        override suspend fun getAccount(): SipAccount? = account
        override suspend fun insertAccount(sipAccount: SipAccount) { account = sipAccount }
        override suspend fun clearAccount() { account = null }
    }

    private class FakeBatteryOptimizationProvider : BatteryOptimizationProvider {
        override var isIgnoringBatteryOptimizations: Boolean = true
    }
}
