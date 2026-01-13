package com.kalim.agentdirectory.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Network connectivity monitor that tracks device network status.
 * 
 * Provides:
 * - Synchronous check: isOnline property
 * - Reactive updates: networkStatus Flow
 * 
 * Uses ConnectivityManager.NetworkCallback for real-time network state changes.
 * Validates both internet capability and network validation to ensure actual connectivity.
 */
class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Synchronous check for current network connectivity.
     * 
     * @return true if device has active, validated internet connection
     * 
     * Checks:
     * - Active network exists
     * - Network has internet capability
     * - Network is validated (actually working, not just connected)
     */
    val isOnline: Boolean
        get() {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

    /**
     * Flow that emits network connectivity status changes.
     * 
     * Emits:
     * - true when network becomes available and validated
     * - false when network is lost
     * - Initial status immediately on collection
     * 
     * Automatically cleans up NetworkCallback when Flow is cancelled.
     */
    val networkStatus: Flow<Boolean> = callbackFlow {
        // Network callback that responds to connectivity changes
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network became available
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Network connection lost
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Network capabilities changed (e.g., validated status)
                // Only emit true if network has both internet and validation
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isConnected)
            }
        }

        // Register callback to monitor network changes
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Send initial network status immediately
        trySend(isOnline)

        // Clean up callback when Flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

