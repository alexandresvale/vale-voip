package com.valevoip.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsTrackerTest {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var tracker: FirebaseAnalyticsTracker

    @Before
    fun setUp() {
        firebaseAnalytics = mockk(relaxed = true)
        tracker = FirebaseAnalyticsTracker(firebaseAnalytics)

        // Mockamos o Bundle do Android (Framework) para evitar 'RuntimeException: Stub!' nos testes da JVM
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } answers {}
        every { anyConstructed<Bundle>().putLong(any(), any()) } answers {}
        every { anyConstructed<Bundle>().putDouble(any(), any()) } answers {}
        every { anyConstructed<Bundle>().putFloat(any(), any()) } answers {}
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `logEvent should map AnalyticsEvent primitive types to Bundle correctly`() {
        // Given
        val parameters = mapOf(
            "key_string" to "value",
            "key_int" to 42,
            "key_bool" to true,
            "key_double" to 3.14,
            "key_float" to 1.5f
        )
        val event = AnalyticsEvent("login_click", parameters)

        // When
        tracker.logEvent(event)

        // Then
        // 1. Garante que o método principal foi chamado
        verify(exactly = 1) { firebaseAnalytics.logEvent(eq("login_click"), any()) }

        // 2. Garante que as nossas regras de negócio de conversão (when block) estão corretas
        verify {
            anyConstructed<Bundle>().putString("key_string", "value")
            anyConstructed<Bundle>().putLong("key_int", 42L)
            anyConstructed<Bundle>().putString("key_bool", "true") // Boolean virou String
            anyConstructed<Bundle>().putDouble("key_double", 3.14)
            anyConstructed<Bundle>().putDouble("key_float", 1.5) // Float virou Double
        }
    }

    @Test
    fun `setUserProperty should delegate directly to FirebaseAnalytics`() {
        // When
        tracker.setUserProperty("theme", "dark")

        // Then
        verify(exactly = 1) { firebaseAnalytics.setUserProperty("theme", "dark") }
    }
}
