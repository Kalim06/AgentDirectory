package com.kalim.agentdirectory.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore extension property for settings preferences.
 * Creates a singleton DataStore instance scoped to the application context.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Settings manager using DataStore for persistent app preferences.
 * 
 * Manages:
 * - Offline-only mode toggle
 * - Background auto-refresh toggle
 * - Last refresh timestamp
 * 
 * All settings are exposed as Flow for reactive UI updates.
 * Uses DataStore (replacement for SharedPreferences) for type-safe, async storage.
 */
class SettingsManager(private val context: Context) {
    companion object {
        // Preference keys for DataStore
        private val OFFLINE_ONLY_MODE = booleanPreferencesKey("offline_only_mode")
        private val AUTO_REFRESH_ENABLED = booleanPreferencesKey("auto_refresh_enabled")
        private val LAST_REFRESH_TIME = longPreferencesKey("last_refresh_time")
    }

    /**
     * Flow of offline-only mode setting.
     * Defaults to false (network allowed).
     */
    val offlineOnlyMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[OFFLINE_ONLY_MODE] ?: false
    }

    /**
     * Flow of auto-refresh enabled setting.
     * Defaults to true (auto-refresh enabled by default).
     */
    val autoRefreshEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_REFRESH_ENABLED] ?: true
    }

    /**
     * Flow of last refresh timestamp.
     * Returns 0L if never refreshed.
     */
    val lastRefreshTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_REFRESH_TIME] ?: 0L
    }

    /**
     * Update offline-only mode setting.
     * 
     * @param enabled true to enable offline-only mode (blocks all network requests)
     */
    suspend fun setOfflineOnlyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OFFLINE_ONLY_MODE] = enabled
        }
    }

    /**
     * Update auto-refresh enabled setting.
     * 
     * @param enabled true to enable background periodic refresh
     */
    suspend fun setAutoRefreshEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_REFRESH_ENABLED] = enabled
        }
    }

    /**
     * Update the last refresh timestamp to current time.
     * Called after successful network refresh operations.
     */
    suspend fun updateLastRefreshTime() {
        context.dataStore.edit { preferences ->
            preferences[LAST_REFRESH_TIME] = System.currentTimeMillis()
        }
    }
}

