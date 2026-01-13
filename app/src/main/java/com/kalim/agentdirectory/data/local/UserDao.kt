package com.kalim.agentdirectory.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kalim.agentdirectory.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entity operations.
 * 
 * Provides:
 * - Reactive queries using Flow for automatic UI updates
 * - Search functionality with LIKE queries
 * - Batch insert operations
 * - Conflict resolution (REPLACE strategy)
 */
@Dao
interface UserDao {
    /**
     * Gets all users ordered by first name.
     * Returns Flow for reactive updates when database changes.
     */
    @Query("SELECT * FROM users ORDER BY firstName ASC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Gets a single user by ID.
     * Used for instant profile display from cache.
     * 
     * @param userId The ID of the user to retrieve
     * @return User if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    /**
     * Searches users by query string.
     * Searches in firstName, lastName, email, and username fields.
     * Returns Flow for reactive search results.
     * 
     * @param query Search query string
     * @return Flow of matching users
     */
    @Query("SELECT * FROM users WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY firstName ASC")
    fun searchUsers(query: String): Flow<List<User>>

    /**
     * Inserts or replaces a list of users.
     * Uses REPLACE strategy to update existing users.
     * 
     * @param users List of users to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    /**
     * Inserts or replaces a single user.
     * 
     * @param user User to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Deletes all users from the database.
     * Used for cache clearing operations.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    /**
     * Gets the total count of users in the database.
     * 
     * @return Number of users stored
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

