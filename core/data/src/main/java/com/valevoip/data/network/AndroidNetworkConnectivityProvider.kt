package com.valevoip.data.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import com.valevoip.core.domain.client.NetworkConnectivityProvider
import com.valevoip.core.domain.client.NetworkConnectivityState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNetworkConnectivityProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkConnectivityProvider {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    @get:RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override val isWifiConnected: Boolean
        get() {
            val cm = connectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    @get:RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override val isConnected: Boolean
        get() {
            val cm = connectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    @get:RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override val connectivityState: Flow<NetworkConnectivityState> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(NetworkConnectivityState(isConnected = false, isWifi = false))
            close()
            return@callbackFlow
        }

        // Emite o estado inicial imediatamente
        trySend(
            NetworkConnectivityState(
                isConnected = isConnected,
                isWifi = isWifiConnected
            )
        )

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Rede conectada: $network")
                trySend(
                    NetworkConnectivityState(
                        isConnected = isConnected,
                        isWifi = isWifiConnected
                    )
                )
            }

            override fun onLost(network: Network) {
                Timber.d("Rede perdida: $network")
                trySend(
                    NetworkConnectivityState(
                        isConnected = isConnected,
                        isWifi = isWifiConnected
                    )
                )
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(
                    NetworkConnectivityState(
                        isConnected = isConnected,
                        isWifi = isWifiConnected
                    )
                )
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao registrar NetworkCallback no ConnectivityManager")
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Timber.e(e, "Falha ao desregistrar NetworkCallback")
            }
        }
    }.distinctUntilChanged().conflate()
}
