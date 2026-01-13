package com.kalim.agentdirectory.data.api

import com.kalim.agentdirectory.data.model.PostsResponse
import com.kalim.agentdirectory.data.model.UsersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service interface for dummyjson.com API.
 * 
 * All endpoints return Response wrapper for error handling.
 * Uses suspend functions for coroutine-based async operations.
 */
interface ApiService {
    /**
     * Fetches paginated list of users.
     * 
     * @param limit Maximum number of users to return (default: 20)
     * @param skip Number of users to skip for pagination (default: 0)
     * @return Response containing UsersResponse with list of users
     */
    @GET("users")
    suspend fun getUsers(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<UsersResponse>

    /**
     * Searches users by query string.
     * 
     * @param query Search query (searches in name, email, username)
     * @return Response containing UsersResponse with matching users
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<UsersResponse>

    /**
     * Fetches posts for a specific user.
     * 
     * @param userId The ID of the user whose posts to fetch
     * @return Response containing PostsResponse with list of posts
     */
    @GET("posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: Int
    ): Response<PostsResponse>
}

