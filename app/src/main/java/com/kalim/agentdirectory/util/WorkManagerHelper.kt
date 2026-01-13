package com.kalim.agentdirectory.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kalim.agentdirectory.ui.work.RefreshWorker
import java.util.concurrent.TimeUnit

/**
 * Helper object for managing WorkManager periodic refresh tasks.
 * 
 * Provides utilities for:
 * - Scheduling periodic background refresh
 * - Canceling refresh work
 * - Managing work constraints for battery optimization
 */
object WorkManagerHelper {
    /** Unique work name to prevent duplicate periodic work */
    private const val REFRESH_WORK_NAME = "agent_refresh_work"

    /**
     * Schedules periodic background refresh every 15 minutes.
     * 
     * Constraints:
     * - Requires network connection (won't run offline)
     * - Requires battery not low (respects battery saver)
     * 
     * Uses ExistingPeriodicWorkPolicy.KEEP to avoid rescheduling if work already exists.
     * 
     * @param context Application context
     */
    fun schedulePeriodicRefresh(context: Context) {
        // Define constraints for battery optimization
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when online
            .setRequiresBatteryNotLow(true) // Don't run when battery is low
            .build()

        // Create periodic work request (runs every 15 minutes)
        val refreshWork = PeriodicWorkRequestBuilder<RefreshWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // Enqueue unique periodic work (prevents duplicates)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            refreshWork
        )
    }

    /**
     * Cancels the periodic refresh work.
     * 
     * @param context Application context
     */
    fun cancelPeriodicRefresh(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REFRESH_WORK_NAME)
    }
}

