package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.SipClient
import com.valevoip.core.domain.model.CallStatus
import com.valevoip.core.domain.model.SipAccount
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.core.domain.model.SipTransport
import com.valevoip.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterAccountUseCaseTest {

    private lateinit var fakeSipClient: FakeSipClient
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var registerAccountUseCase: RegisterAccountUseCase

    @Before
    fun setUp() {
        fakeSipClient = FakeSipClient()
        fakeAccountRepository = FakeAccountRepository()
        registerAccountUseCase = RegisterAccountUseCase(fakeSipClient, fakeAccountRepository)
    }

    @Test
    fun `should return EmptyUsername error when username is blank`() = runTest {
        val result = registerAccountUseCase("", "pass", "sip.valevoip.com.br")

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.EmptyUsername, (result as RegisterResult.Error).error)
    }

    @Test
    fun `should return EmptyPassword error when password is blank`() = runTest {
        val result = registerAccountUseCase("1001", "", "sip.valevoip.com.br")

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.EmptyPassword, (result as RegisterResult.Error).error)
    }

    @Test
    fun `should return EmptyDomain error when domain is blank`() = runTest {
        val result = registerAccountUseCase("1001", "pass", "")

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.EmptyDomain, (result as RegisterResult.Error).error)
    }

    @Test
    fun `should return InvalidDomain error when domain is malformed`() = runTest {
        val result = registerAccountUseCase("1001", "pass", "invalid_domain")

        assertTrue(result is RegisterResult.Error)
        assertEquals(RegisterError.InvalidDomain, (result as RegisterResult.Error).error)
    }

    @Test
    fun `should succeed and save account when domain is valid FQDN and SIP registers successfully`() = runTest {
        fakeSipClient.registrationEmission = flowOf(SipRegistrationState.Progress, SipRegistrationState.Ok)

        val result = registerAccountUseCase("1001", "pass123", "pbx.valevoip.com.br", SipTransport.TLS)

        assertTrue(result is RegisterResult.Success)
        assertEquals("1001", fakeAccountRepository.account?.username)
        assertEquals("pass123", fakeAccountRepository.account?.password)
        assertEquals("pbx.valevoip.com.br", fakeAccountRepository.account?.domain)
        assertEquals(SipTransport.TLS, fakeAccountRepository.account?.transport)
    }

    @Test
    fun `should succeed when host is a valid IPv4 address`() = runTest {
        fakeSipClient.registrationEmission = flowOf(SipRegistrationState.Ok)

        val result = registerAccountUseCase("1001", "pass123", "192.168.1.100")

        assertTrue(result is RegisterResult.Success)
        assertEquals("192.168.1.100", fakeAccountRepository.account?.domain)
    }

    @Test
    fun `should return SipClientError when registration fails`() = runTest {
        fakeSipClient.registrationEmission = flowOf(
            SipRegistrationState.Failed(reason = "Forbidden")
        )

        val result = registerAccountUseCase("1001", "wrong_pass", "pbx.valevoip.com.br")

        assertTrue(result is RegisterResult.Error)
        val error = (result as RegisterResult.Error).error
        assertTrue(error is RegisterError.SipClientError)
        assertEquals("Forbidden", (error as RegisterError.SipClientError).cause.message)
        assertEquals(null, fakeAccountRepository.account)
    }

    private class FakeSipClient : SipClient {
        var registrationEmission: Flow<SipRegistrationState> = emptyFlow()

        override val registrationState: StateFlow<SipRegistrationState> =
            MutableStateFlow(SipRegistrationState.None).asStateFlow()

        override suspend fun register(account: SipAccount): Flow<SipRegistrationState> = registrationEmission
        override suspend fun unregister(): Flow<SipRegistrationState> = emptyFlow()
        override fun makeCall(number: String): Result<Unit> = Result.success(Unit)
        override fun hangUp(): Result<Unit> = Result.success(Unit)
        override fun toggleMute(): Result<Unit> = Result.success(Unit)
        override fun toggleSpeaker(): Result<Boolean> = Result.success(true)
        override fun muteCall(callId: String): Result<Unit> = Result.success(Unit)
        override fun acceptCall(): Result<Unit> = Result.success(Unit)
        override fun getCallStatusFlow(): Flow<CallStatus> = emptyFlow()
        override fun getCurrentCallNumber(): String? = null
        override fun getSynchronousCallStatus(): CallStatus = CallStatus.IDLE
        override fun getLinphoneVersion(): String = "5.0.0"
    }

    private class FakeAccountRepository : AccountRepository {
        var account: SipAccount? = null
        override suspend fun getAccount(): SipAccount? = account
        override suspend fun insertAccount(sipAccount: SipAccount) { account = sipAccount }
        override suspend fun clearAccount() { account = null }
    }
}
