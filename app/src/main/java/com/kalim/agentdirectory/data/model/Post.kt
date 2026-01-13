package com.kalim.agentdirectory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.kalim.agentdirectory.data.local.Converters

/**
 * Post entity representing a user's post.
 * 
 * Room Entity: Stored in "posts" table for offline caching.
 * The reactions field is stored as JSON string using TypeConverters.
 * 
 * @param id Unique post identifier
 * @param title Post title
 * @param body Post content/body
 * @param userId ID of the user who created the post
 * @param tags List of tags associated with the post
 * @param reactions Reaction counts object (likes, loves, etc.)
 * @param cachedAt Timestamp when this post was cached locally
 */
@Entity(tableName = "posts")
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey
    val id: Int,
    val title: String,
    val body: String,
    @SerializedName("userId")
    val userId: Int,
    val tags: List<String>?,
    val reactions: Reactions?, // API returns object, not int
    @SerializedName("cachedAt")
    val cachedAt: Long = System.currentTimeMillis()
) {
    /**
     * Computed property that sums all reaction types.
     * Used for displaying total reaction count in UI.
     */
    val totalReactions: Int
        get() = reactions?.let {
            (it.likes ?: 0) + (it.loves ?: 0) + (it.haha ?: 0) + 
            (it.wow ?: 0) + (it.sad ?: 0) + (it.angry ?: 0)
        } ?: 0
}

/**
 * Reactions data class representing different reaction types.
 * 
 * The API returns reactions as an object with individual reaction counts,
 * not as a single integer. This structure matches the API response format.
 */
data class Reactions(
    val likes: Int? = null,
    val loves: Int? = null,
    val haha: Int? = null,
    val wow: Int? = null,
    val sad: Int? = null,
    val angry: Int? = null
)

