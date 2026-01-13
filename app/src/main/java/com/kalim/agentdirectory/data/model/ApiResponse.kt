package com.kalim.agentdirectory.data.model

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    val users: List<User>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

data class PostsResponse(
    val posts: List<Post>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

