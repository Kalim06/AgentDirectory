package com.kalim.agentdirectory.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kalim.agentdirectory.data.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Post entity operations.
 * 
 * Provides:
 * - Reactive queries using Flow for automatic UI updates
 * - Post retrieval by user ID
 * - Batch insert operations
 * - Cache management operations
 */
@Dao
interface PostDao {
    /**
     * Gets all posts for a specific user, ordered by ID descending (newest first).
     * Returns Flow for reactive updates when posts are added/updated.
     * 
     * @param userId The ID of the user whose posts to retrieve
     * @return Flow of posts for the user
     */
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY id DESC")
    fun getPostsByUserId(userId: Int): Flow<List<Post>>

    /**
     * Inserts or replaces a list of posts.
     * Uses REPLACE strategy to update existing posts.
     * 
     * @param posts List of posts to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    /**
     * Deletes all posts for a specific user.
     * Used for cache invalidation when user data is refreshed.
     * 
     * @param userId The ID of the user whose posts to delete
     */
    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deletePostsByUserId(userId: Int)

    /**
     * Deletes all posts from the database.
     * Used for cache clearing operations.
     */
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
}

