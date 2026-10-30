package com.valevoip.core.domain.client

import kotlinx.coroutines.flow.Flow

/**
 * Abstração pura de domínio para verificação e observação do estado de conectividade de rede.
 * Isola a regra de negócio do framework Android (ConnectivityManager).
 */
interface NetworkConnectivityProvider {
    val isWifiConnected: Boolean
    val isConnected: Boolean
    val connectivityState: Flow<NetworkConnectivityState>
}

data class NetworkConnectivityState(
    val isConnected: Boolean,
    val isWifi: Boolean
)
