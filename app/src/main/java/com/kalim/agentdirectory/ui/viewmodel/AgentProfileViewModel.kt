package com.kalim.agentdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalim.agentdirectory.data.model.Post
import com.kalim.agentdirectory.data.model.User
import com.kalim.agentdirectory.data.repository.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Agent Profile screen.
 * 
 * Responsibilities:
 * - Manages user profile data and posts
 * - Implements cache-first loading for instant display
 * - Handles loading and error states
 * - Provides refresh functionality
 * 
 * @param repository Repository for data operations
 * @param userId ID of the user whose profile is being displayed
 */
class AgentProfileViewModel(
    private val repository: AgentRepository,
    private val userId: Int
) : ViewModel() {

    // Private mutable state flows
    private val _user = MutableStateFlow<User?>(null)
    /** Public immutable StateFlow for observing user data in UI */
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    /** Public immutable StateFlow for observing posts list in UI */
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** Loading state for showing progress indicators */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error state for displaying error messages */
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load user and posts immediately when ViewModel is created
        loadUser()
        loadPosts()
    }

    /**
     * Loads user data using cache-first strategy.
     * 
     * Flow:
     * 1. Loads from cache instantly (for immediate UI display)
     * 2. Refreshes from network in background
     * 3. Updates UI when network data arrives
     */
    private fun loadUser() {
        viewModelScope.launch {
            // Load from cache first for instant display (no waiting for network)
            val cachedUser = repository.getUserById(userId)
            _user.value = cachedUser

            // Try to refresh from network in background (non-blocking)
            // Updates cache, which will trigger Flow emission if user is in the list
            repository.refreshUsersFromNetwork().fold(
                onSuccess = { users ->
                    // Find the specific user in the refreshed list
                    val updatedUser = users.find { it.id == userId }
                    if (updatedUser != null) {
                        _user.value = updatedUser
                    }
                },
                onFailure = { /* Silent failure, use cached data */ }
            )
        }
    }

    /**
     * Loads posts using cache-first strategy with reactive updates.
     * 
     * Flow:
     * 1. Observes Room Flow for instant cached posts
     * 2. Triggers background network refresh
     * 3. Flow automatically emits when database updates
     */
    private fun loadPosts() {
        // Observe posts flow from Room database (cache-first)
        // Automatically updates UI when posts are added/updated in database
        viewModelScope.launch {
            repository.getUserPostsFlow(userId).collect { postList ->
                _posts.value = postList
                _isLoading.value = false
            }
        }

        // Refresh from network in background (non-blocking)
        // Updates Room database, which triggers Flow emission above
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.refreshUserPosts(userId).fold(
                onSuccess = { /* Already handled by flow */ },
                onFailure = { e ->
                    // Only show error if we have no cached data
                    if (_posts.value.isEmpty()) {
                        _error.value = e.message
                    }
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Manually refresh posts from network (pull-to-refresh).
     * 
     * Shows loading indicator and error message if refresh fails.
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.refreshUserPosts(userId).fold(
                onSuccess = { /* Already handled by flow */ },
                onFailure = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        }
    }
}

