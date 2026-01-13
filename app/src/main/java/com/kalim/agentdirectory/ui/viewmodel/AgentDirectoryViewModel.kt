package com.kalim.agentdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalim.agentdirectory.data.model.User
import com.kalim.agentdirectory.data.repository.AgentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the Agent Directory screen (home screen).
 * 
 * Responsibilities:
 * - Manages list of agents displayed in RecyclerView
 * - Handles search functionality with debouncing
 * - Coordinates data loading from repository
 * - Manages loading and error states
 * 
 * State Management:
 * - Uses StateFlow for reactive UI updates
 * - Exposes immutable StateFlow to UI layer
 * - Handles search debouncing to minimize API calls
 */
class AgentDirectoryViewModel(
    private val repository: AgentRepository
) : ViewModel() {

    // Private mutable state flows (internal state)
    private val _users = MutableStateFlow<List<User>>(emptyList())
    /** Public immutable StateFlow for observing users list in UI */
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** Loading state for showing progress indicators */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error state for displaying error messages */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /** Current search query for tracking user input */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Job for canceling previous network search requests */
    private var searchJob: Job? = null

    init {
        // Load initial data and set up search observer on ViewModel creation
        loadUsers()
        observeSearch()
    }

    /**
     * Loads users from repository using cache-first strategy.
     * 
     * Flow:
     * 1. Observes Room Flow for instant cached data display
     * 2. Triggers background network refresh to update cache
     */
    private fun loadUsers() {
        // Observe users flow from Room database (cache-first)
        // This provides instant UI updates when data changes
        viewModelScope.launch {
            repository.getAllUsersFlow().collect { userList ->
                _users.value = userList
                _isLoading.value = false
            }
        }

        // Refresh from network in background (non-blocking)
        // Updates cache silently, which triggers Flow emission above
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshUsersFromNetwork().fold(
                onSuccess = { /* Already handled by flow */ },
                onFailure = { /* Silent failure, use cached data */ }
            )
        }
    }

    /**
     * Observes search query changes and updates user list reactively.
     * 
     * Features:
     * - 500ms debounce to avoid excessive database queries
     * - Distinct until changed to prevent duplicate searches
     * - FlatMapLatest cancels previous search when new query arrives
     * - Shows cached results instantly, then updates from network
     */
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Wait 500ms after user stops typing
                .distinctUntilChanged() // Only proceed if query actually changed
                .flatMapLatest { query ->
                    // Cancel previous search and start new one
                    if (query.isBlank()) {
                        // Show all users when search is cleared
                        repository.getAllUsersFlow()
                    } else {
                        // Show cached search results instantly
                        repository.searchUsersFlow(query)
                    }
                }
                .collect { users ->
                    // Update UI with search results
                    _users.value = users
                }
        }
    }

    /**
     * Called when user types in search field.
     * 
     * @param query The search query string
     * 
     * Behavior:
     * - Updates search query StateFlow (triggers observeSearch)
     * - Cancels previous network search job
     * - Triggers network search after debounce for fresh results
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        // Cancel previous search job to avoid race conditions
        searchJob?.cancel()

        // If query is not empty, search from network after debounce
        // This ensures we get latest results from API, not just cache
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(500) // Debounce to match Flow debounce
                repository.searchUsersFromNetwork(query).fold(
                    onSuccess = { /* Already handled by flow */ },
                    onFailure = { /* Silent failure, use cached data */ }
                )
            }
        }
    }

    /**
     * Manually refresh data from network (pull-to-refresh).
     * 
     * Behavior:
     * - Shows loading indicator
     * - Refreshes current view (all users or search results)
     * - Displays error if refresh fails
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Refresh based on current search state
            val query = _searchQuery.value
            val result = if (query.isBlank()) {
                repository.refreshUsersFromNetwork()
            } else {
                repository.searchUsersFromNetwork(query)
            }

            result.fold(
                onSuccess = { /* Already handled by flow */ },
                onFailure = { e ->
                    _error.value = e.message
                }
            )

            _isLoading.value = false
        }
    }
}

