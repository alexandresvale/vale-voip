package com.valevoip.core.analytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class CrashlyticsTreeTest {

    private lateinit var crashlyticsMock: FirebaseCrashlytics
    private lateinit var tree: CrashlyticsTree

    @Before
    fun setUp() {
        crashlyticsMock = mockk(relaxed = true)
        
        // Mockamos a chamada estática FirebaseCrashlytics.getInstance()
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlyticsMock
        
        tree = CrashlyticsTree()
        Timber.plant(tree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `log with VERBOSE priority should be ignored`() {
        // When
        Timber.tag("TAG").v("Verbose message")

        // Then
        verify(exactly = 0) { crashlyticsMock.log(any()) }
        verify(exactly = 0) { crashlyticsMock.recordException(any()) }
    }

    @Test
    fun `log with DEBUG priority should be ignored`() {
        // When
        Timber.tag("TAG").d("Debug message")

        // Then
        verify(exactly = 0) { crashlyticsMock.log(any()) }
        verify(exactly = 0) { crashlyticsMock.recordException(any()) }
    }

    @Test
    fun `log with INFO priority should log breadcrumb to Crashlytics`() {
        // When
        Timber.tag("SIP").i("Registered successfully")

        // Then
        // O Breadcrumb deve ter a tag formatada: "[SIP] Registered successfully"
        verify(exactly = 1) { crashlyticsMock.log("[SIP] Registered successfully") }
        verify(exactly = 0) { crashlyticsMock.recordException(any()) }
    }

    @Test
    fun `log with ERROR priority and Exception should log breadcrumb and record Exception`() {
        // Given
        val exception = RuntimeException("Connection Failed")

        // When
        Timber.tag("Network").e(exception, "Lost connection")

        // Then
        verify(exactly = 1) { crashlyticsMock.log(match { it.startsWith("[Network] Lost connection") }) }
        verify(exactly = 1) { crashlyticsMock.recordException(exception) }
    }
}
