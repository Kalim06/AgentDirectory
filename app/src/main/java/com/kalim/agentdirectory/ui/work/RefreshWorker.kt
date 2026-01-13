package com.kalim.agentdirectory.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kalim.agentdirectory.AgentDirectoryApplication
import com.kalim.agentdirectory.data.repository.AgentRepository
import com.kalim.agentdirectory.util.SettingsManager
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for periodic background data refresh.
 * 
 * Responsibilities:
 * - Periodically refreshes user data from network
 * - Respects app settings (offline mode, auto-refresh toggle)
 * - Handles retries on failure
 * 
 * Lifecycle:
 * - Runs every 15 minutes when enabled
 * - Automatically paused when app is in background or offline mode enabled
 * - Respects battery optimization constraints
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Performs the background refresh work.
     * 
     * @return Result.success() if work completed successfully
     * @return Result.retry() if work should be retried later
     * 
     * Behavior:
     * - Checks if auto-refresh is enabled (returns success if disabled)
     * - Checks if offline-only mode is enabled (returns success if enabled)
     * - Attempts to refresh users from network
     * - Retries on failure (WorkManager handles exponential backoff)
     */
    override suspend fun doWork(): Result {
        val app = applicationContext as AgentDirectoryApplication
        val repository = app.repository
        val settingsManager = app.settingsManager

        // Check if auto-refresh is enabled in settings
        val autoRefreshEnabled = settingsManager.autoRefreshEnabled.first()
        if (!autoRefreshEnabled) {
            // Auto-refresh disabled, exit gracefully
            return Result.success()
        }

        // Check if offline-only mode is enabled
        val offlineOnly = settingsManager.offlineOnlyMode.first()
        if (offlineOnly) {
            // Offline mode enabled, skip network refresh
            return Result.success()
        }

        // Attempt to refresh data from network
        return try {
            repository.refreshUsersFromNetwork().fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() } // WorkManager will retry with backoff
            )
        } catch (e: Exception) {
            // Retry on any exception
            Result.retry()
        }
    }
}

