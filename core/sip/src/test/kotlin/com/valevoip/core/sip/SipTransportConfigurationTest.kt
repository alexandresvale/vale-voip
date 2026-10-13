package com.valevoip.core.sip

import com.valevoip.core.domain.model.SipTransport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.linphone.core.TransportType

internal class SipTransportConfigurationTest {

    @Test
    fun `TCP selects Linphone TCP and does not weaken certificate validation`() {
        val configuration = sipTransportConfiguration(SipTransport.TCP)

        assertEquals(TransportType.Tcp, configuration.linphoneTransport)
        assertTrue(configuration.verifyServerCertificates)
        assertTrue(configuration.verifyServerCommonName)
    }

    @Test
    fun `TLS selects Linphone TLS and requires certificate and server name validation`() {
        val configuration = sipTransportConfiguration(SipTransport.TLS)

        assertEquals(TransportType.Tls, configuration.linphoneTransport)
        assertTrue(configuration.verifyServerCertificates)
        assertTrue(configuration.verifyServerCommonName)
    }
}
