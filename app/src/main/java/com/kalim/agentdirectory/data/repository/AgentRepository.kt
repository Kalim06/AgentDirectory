package com.kalim.agentdirectory.data.repository

import com.kalim.agentdirectory.data.api.ApiService
import com.kalim.agentdirectory.data.local.AppDatabase
import com.kalim.agentdirectory.data.model.Post
import com.kalim.agentdirectory.data.model.User
import com.kalim.agentdirectory.util.NetworkMonitor
import com.kalim.agentdirectory.util.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Repository class that implements the single source of truth pattern.
 * 
 * Responsibilities:
 * - Coordinates data fetching from network (API) and local storage (Room)
 * - Implements cache-first strategy for instant UI updates
 * - Handles offline mode and network availability checks
 * - Manages data synchronization between network and local database
 * 
 * Architecture Pattern: Repository Pattern
 * - Abstracts data sources (API, Database) from ViewModels
 * - Provides clean API for data operations
 * - Handles complex caching and offline logic
 */
class AgentRepository(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val networkMonitor: NetworkMonitor,
    private val settingsManager: SettingsManager
) {
    private val userDao = database.userDao()
    private val postDao = database.postDao()

    /**
     * Fetches users from network and updates local cache.
     * 
     * @param limit Maximum number of users to fetch (default: 20)
     * @param skip Number of users to skip for pagination (default: 0)
     * @return Result containing list of users or error
     * 
     * Behavior:
     * - Checks offline mode and network availability before making request
     * - Updates Room database with fetched data
     * - Updates last refresh timestamp in settings
     * - Returns Result for error handling
     */
    suspend fun refreshUsersFromNetwork(limit: Int = 20, skip: Int = 0): Result<List<User>> {
        return try {
            val offlineOnly = settingsManager.offlineOnlyMode.first()
            val isOnline = networkMonitor.isOnline

            if (!isOnline || offlineOnly) {
                return Result.failure(IOException("Network unavailable or offline mode enabled"))
            }

            val response = apiService.getUsers(limit, skip)
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!.users.map { it.copy(cachedAt = System.currentTimeMillis()) }
                userDao.insertUsers(users)
                settingsManager.updateLastRefreshTime()
                Result.success(users)
            } else {
                Result.failure(IOException("Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a Flow of all users from local database.
     * This Flow automatically emits updates when database changes.
     * Used for reactive UI updates with cache-first strategy.
     */
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsers()

    /**
     * Searches users from network API and updates local cache.
     * 
     * @param query Search query string
     * @return Result containing list of matching users or error
     * 
     * Note: Search results are also cached for offline access.
     */
    suspend fun searchUsersFromNetwork(query: String): Result<List<User>> {
        return try {
            val offlineOnly = settingsManager.offlineOnlyMode.first()
            val isOnline = networkMonitor.isOnline

            if (!isOnline || offlineOnly) {
                return Result.failure(IOException("Network unavailable or offline mode enabled"))
            }

            val response = apiService.searchUsers(query)
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!.users.map { it.copy(cachedAt = System.currentTimeMillis()) }
                userDao.insertUsers(users)
                Result.success(users)
            } else {
                Result.failure(IOException("Failed to search users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a Flow of users matching the search query from local database.
     * Provides instant search results from cache while network search happens in background.
     */
    fun searchUsersFlow(query: String): Flow<List<User>> = userDao.searchUsers(query)

    /**
     * Retrieves a single user by ID from local database.
     * Used for instant profile display before network refresh.
     */
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    /**
     * Fetches user posts from network and updates local cache.
     * 
     * @param userId The ID of the user whose posts to fetch
     * @return Result containing list of posts or error
     */
    suspend fun refreshUserPosts(userId: Int): Result<List<Post>> {
        return try {
            val offlineOnly = settingsManager.offlineOnlyMode.first()
            val isOnline = networkMonitor.isOnline

            if (!isOnline || offlineOnly) {
                return Result.failure(IOException("Network unavailable or offline mode enabled"))
            }

            val response = apiService.getUserPosts(userId)
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.posts.map { it.copy(cachedAt = System.currentTimeMillis()) }
                postDao.insertPosts(posts)
                Result.success(posts)
            } else {
                Result.failure(IOException("Failed to fetch posts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a Flow of posts for a specific user from local database.
     * Automatically emits updates when posts are added/updated in database.
     */
    fun getUserPostsFlow(userId: Int): Flow<List<Post>> = postDao.getPostsByUserId(userId)

}

