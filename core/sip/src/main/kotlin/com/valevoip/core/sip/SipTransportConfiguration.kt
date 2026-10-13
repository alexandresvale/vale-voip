package com.valevoip.core.sip

import com.valevoip.core.domain.model.SipTransport
import org.linphone.core.TransportType

internal data class SipTransportConfiguration(
    val linphoneTransport: TransportType,
    val verifyServerCertificates: Boolean,
    val verifyServerCommonName: Boolean
)

internal fun sipTransportConfiguration(transport: SipTransport): SipTransportConfiguration =
    SipTransportConfiguration(
        linphoneTransport = when (transport) {
            SipTransport.TCP -> TransportType.Tcp
            SipTransport.TLS -> TransportType.Tls
        },
        verifyServerCertificates = true,
        verifyServerCommonName = true
    )
