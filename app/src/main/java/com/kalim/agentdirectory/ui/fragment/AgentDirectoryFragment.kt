package com.kalim.agentdirectory.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalim.agentdirectory.AgentDirectoryApplication
import com.kalim.agentdirectory.R
import com.kalim.agentdirectory.data.model.User
import com.kalim.agentdirectory.databinding.FragmentAgentDirectoryBinding
import com.kalim.agentdirectory.ui.adapter.AgentAdapter
import com.kalim.agentdirectory.ui.viewmodel.AgentDirectoryViewModel
import com.kalim.agentdirectory.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

/**
 * Fragment displaying the list of agents (home screen).
 * 
 * Features:
 * - Displays scrollable list of agents in RecyclerView
 * - Search functionality with debouncing
 * - Pull-to-refresh for manual data refresh
 * - Empty state and error handling
 * 
 * Uses ViewBinding for type-safe view access and StateFlow for reactive updates.
 */
class AgentDirectoryFragment : Fragment() {

    private var _binding: FragmentAgentDirectoryBinding? = null
    private val binding get() = _binding!!

    /** RecyclerView adapter for displaying agent list */
    private lateinit var adapter: AgentAdapter
    
    /** ViewModel for managing agent list state and business logic */
    private val viewModel: AgentDirectoryViewModel by viewModels {
        val app = requireActivity().application as AgentDirectoryApplication
        ViewModelFactory(
            app.repository,
            app.settingsManager,
            app.networkMonitor
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgentDirectoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        observeViewModel()
    }

    /**
     * Sets up RecyclerView with adapter and layout manager.
     * Adapter handles item clicks by navigating to profile screen.
     */
    private fun setupRecyclerView() {
        adapter = AgentAdapter { user ->
            navigateToProfile(user)
        }
        binding.recyclerViewAgents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAgents.adapter = adapter
    }

    /**
     * Sets up search functionality with TextWatcher.
     * ViewModel handles debouncing and search logic.
     */
    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Notify ViewModel of search query change (debouncing handled in ViewModel)
                viewModel.onSearchQueryChanged(s?.toString() ?: "")
            }
        })
    }

    /**
     * Sets up pull-to-refresh functionality.
     * Triggers manual data refresh when user pulls down.
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    /**
     * Observes ViewModel StateFlows and updates UI reactively.
     * Uses repeatOnLifecycle to only collect when fragment is started (prevents wasted work).
     */
    private fun observeViewModel() {
        // Observe users list and update RecyclerView
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    adapter.submitList(users) // DiffUtil handles efficient updates
                    binding.textEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe loading state and update progress indicators
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.swipeRefresh.isRefreshing = isLoading
                }
            }
        }

        // Observe error state and display error messages
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    binding.textError.text = error
                    binding.textError.visibility = if (error != null) View.VISIBLE else View.GONE
                }
            }
        }
    }

    /**
     * Navigates to agent profile screen when user taps an agent card.
     * 
     * @param user The user whose profile to display
     */
    private fun navigateToProfile(user: User) {
        val bundle = Bundle().apply {
            putInt("userId", user.id)
        }
        findNavController().navigate(R.id.agentProfileFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear binding reference to prevent memory leaks
        _binding = null
    }
}

