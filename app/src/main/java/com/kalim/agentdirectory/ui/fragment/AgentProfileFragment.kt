package com.kalim.agentdirectory.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import com.kalim.agentdirectory.AgentDirectoryApplication
import com.kalim.agentdirectory.databinding.FragmentAgentProfileBinding
import com.kalim.agentdirectory.ui.adapter.PostAdapter
import com.kalim.agentdirectory.ui.viewmodel.AgentProfileViewModel
import com.kalim.agentdirectory.ui.viewmodel.AgentProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Fragment displaying detailed agent profile and their recent posts.
 * 
 * Features:
 * - Displays agent information (name, email, phone, address, company)
 * - Shows agent avatar with Glide image loading
 * - Lists recent posts in RecyclerView
 * - Shimmer loading placeholders for better UX
 * - Cache-first loading for instant display
 * 
 * Navigation:
 * - Receives userId via navigation arguments
 * - Back button handled automatically by Navigation Component
 */
class AgentProfileFragment : Fragment() {

    private var _binding: FragmentAgentProfileBinding? = null
    private val binding get() = _binding!!

    /** RecyclerView adapter for displaying posts list */
    private lateinit var postAdapter: PostAdapter
    
    /** ViewModel for managing profile data and posts */
    private val viewModel: AgentProfileViewModel by viewModels {
        // Get userId from navigation arguments
        val userId = arguments?.getInt("userId") ?: 0
        val app = requireActivity().application as AgentDirectoryApplication
        AgentProfileViewModelFactory(app.repository, userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = postAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    if (user != null) {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        binding.layoutProfile.visibility = View.VISIBLE

                        binding.textName.text = user.fullName
                        binding.textEmail.text = user.email
                        binding.textPhone.text = user.phone
                        val companyInfo = buildString {
                            append("Company: ${user.company?.name ?: "N/A"}")
                            user.company?.department?.let { append("\nDepartment: $it") }
                            user.company?.title?.let { append("\nTitle: $it") }
                        }
                        binding.textCompany.text = companyInfo

                        val address = user.address
                        binding.textAddress.text = if (address != null) {
                            "${address.address}, ${address.city}, ${address.state} ${address.postalCode}"
                        } else {
                            "Address not available"
                        }

                        Glide.with(requireContext())
                            .load(user.image)
                            .circleCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(binding.imageAvatar)
                    } else {
                        binding.shimmerLayout.startShimmer()
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.layoutProfile.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.posts.collect { posts ->
                    postAdapter.submitList(posts)
                    binding.textPostsTitle.visibility = if (posts.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    binding.textError.text = error
                    binding.textError.visibility = if (error != null) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

