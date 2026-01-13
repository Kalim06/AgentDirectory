package com.kalim.agentdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalim.agentdirectory.util.NetworkMonitor
import com.kalim.agentdirectory.util.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * 
 * Responsibilities:
 * - Manages app settings (offline mode, auto-refresh)
 * - Displays network connectivity status
 * - Shows last refresh timestamp
 * - Updates settings in DataStore
 * 
 * Converts Flow to StateFlow using stateIn for efficient sharing across collectors.
 * Uses WhileSubscribed(5000) to keep Flow active for 5 seconds after last subscriber.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    /**
     * Offline-only mode setting as StateFlow.
     * Converts Flow to StateFlow for efficient sharing.
     * Stays active for 5 seconds after last subscriber unsubscribes.
     */
    val offlineOnlyMode: StateFlow<Boolean> = settingsManager.offlineOnlyMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
            initialValue = false
        )

    /**
     * Auto-refresh enabled setting as StateFlow.
     * Defaults to true (auto-refresh enabled by default).
     */
    val autoRefreshEnabled: StateFlow<Boolean> = settingsManager.autoRefreshEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Last refresh timestamp as StateFlow.
     * Shows when data was last successfully refreshed from network.
     */
    val lastRefreshTime: StateFlow<Long> = settingsManager.lastRefreshTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    /**
     * Network connectivity status as StateFlow.
     * Updates in real-time when network status changes.
     */
    val networkStatus: StateFlow<Boolean> = networkMonitor.networkStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Update offline-only mode setting.
     * 
     * @param enabled true to enable offline-only mode (blocks all network requests)
     */
    fun setOfflineOnlyMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setOfflineOnlyMode(enabled)
        }
    }

    /**
     * Update auto-refresh enabled setting.
     * 
     * @param enabled true to enable background periodic refresh
     * 
     * Note: WorkManager scheduling/cancellation should be handled in Fragment
     * when this setting changes.
     */
    fun setAutoRefreshEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setAutoRefreshEnabled(enabled)
        }
    }
}

