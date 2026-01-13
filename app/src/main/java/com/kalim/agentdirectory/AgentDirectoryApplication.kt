package com.kalim.agentdirectory

import android.app.Application
import com.kalim.agentdirectory.data.api.NetworkModule
import com.kalim.agentdirectory.data.local.AppDatabase
import com.kalim.agentdirectory.data.repository.AgentRepository
import com.kalim.agentdirectory.util.NetworkMonitor
import com.kalim.agentdirectory.util.SettingsManager
import com.kalim.agentdirectory.util.WorkManagerHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Application class that provides dependency injection for the entire app.
 * 
 * This class acts as a simple DI container, providing singleton instances of:
 * - Database (Room)
 * - Repository (data access layer)
 * - SettingsManager (DataStore preferences)
 * - NetworkMonitor (connectivity monitoring)
 * 
 * All dependencies are created lazily to ensure they're only initialized when needed.
 */
class AgentDirectoryApplication : Application() {

    /**
     * Room database instance - provides local data persistence.
     * Created lazily to avoid initialization overhead at app startup.
     */
    val database by lazy { AppDatabase.getDatabase(this) }
    
    /**
     * Repository instance - handles all data operations (network + local cache).
     * Coordinates between API service, Room database, and settings.
     */
    val repository by lazy {
        AgentRepository(
            NetworkModule.apiService,
            database,
            NetworkMonitor(this),
            SettingsManager(this)
        )
    }
    
    /**
     * Settings manager - handles app preferences using DataStore.
     * Manages offline mode, auto-refresh settings, and last refresh timestamp.
     */
    val settingsManager by lazy { SettingsManager(this) }
    
    /**
     * Network monitor - tracks device connectivity status.
     * Provides real-time network state updates via Flow.
     */
    val networkMonitor by lazy { NetworkMonitor(this) }

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager for background refresh if enabled in settings
        // This runs synchronously at app startup to restore background work after app restart
        runBlocking {
            val autoRefreshEnabled = settingsManager.autoRefreshEnabled.first()
            if (autoRefreshEnabled) {
                WorkManagerHelper.schedulePeriodicRefresh(this@AgentDirectoryApplication)
            }
        }
    }
}

