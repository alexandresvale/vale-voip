package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelephonyUseCasesTest {

    private lateinit var fakeSipClient: FakeSipClient
    private lateinit var fakeAccountRepository: FakeAccountRepository

    private lateinit var answerCallUseCase: AnswerCallUseCase
    private lateinit var hangUpUseCase: HangUpUseCase
    private lateinit var toggleMuteUseCase: ToggleMuteUseCase
    private lateinit var toggleSpeakerUseCase: ToggleSpeakerUseCase
    private lateinit var getCallStatusSyncUseCase: GetCallStatusSyncUseCase
    private lateinit var getCurrentCallNumberUseCase: GetCurrentCallNumberUseCase
    private lateinit var observeCallStateUseCase: ObserveCallStateUseCase
    private lateinit var getAccountUseCase: GetAccountUseCase
    private lateinit var unregisterAccountUseCase: UnregisterAccountUseCase
    private lateinit var observeRegistrationStateUseCase: ObserveRegistrationStateUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        fakeSipClient = FakeSipClient()
        fakeAccountRepository = FakeAccountRepository()

        answerCallUseCase = AnswerCallUseCase(fakeSipClient)
        hangUpUseCase = HangUpUseCase(fakeSipClient)
        toggleMuteUseCase = ToggleMuteUseCase(fakeSipClient)
        toggleSpeakerUseCase = ToggleSpeakerUseCase(fakeSipClient)
        getCallStatusSyncUseCase = GetCallStatusSyncUseCase(fakeSipClient)
        getCurrentCallNumberUseCase = GetCurrentCallNumberUseCase(fakeSipClient)
        observeCallStateUseCase = ObserveCallStateUseCase(fakeSipClient)
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
        var currentStatus = CallStatus.IDLE
        var currentNumber: String? = null
        val callStatusFlow = MutableStateFlow(CallStatus.IDLE)
        val registrationStateFlow = MutableStateFlow<SipRegistrationState>(SipRegistrationState.None)
        var unregisterEmission: Flow<SipRegistrationState> = emptyFlow()
        var unregisterShouldThrow = false

        override val registrationState: StateFlow<SipRegistrationState> = registrationStateFlow
        override suspend fun register(account: SipAccount): Flow<SipRegistrationState> = emptyFlow()
        override suspend fun unregister(): Flow<SipRegistrationState> {
            if (unregisterShouldThrow) throw IllegalStateException("Network Error")
            return unregisterEmission
        }
        override fun makeCall(number: String): Result<Unit> = Result.success(Unit)
        override fun hangUp(): Result<Unit> { hungUp = true; return Result.success(Unit) }
        override fun toggleMute(): Result<Unit> { mutedToggled = true; return Result.success(Unit) }
        override fun toggleSpeaker(): Result<Boolean> { speakerToggled = true; return Result.success(true) }
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
}
