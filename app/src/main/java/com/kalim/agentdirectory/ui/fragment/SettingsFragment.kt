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
import com.kalim.agentdirectory.AgentDirectoryApplication
import kotlinx.coroutines.launch
import com.kalim.agentdirectory.databinding.FragmentSettingsBinding
import com.kalim.agentdirectory.ui.viewmodel.SettingsViewModel
import com.kalim.agentdirectory.ui.viewmodel.ViewModelFactory
import com.kalim.agentdirectory.util.WorkManagerHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for app settings and configuration.
 * 
 * Features:
 * - Offline-only mode toggle
 * - Background auto-refresh toggle
 * - Network connectivity status display
 * - Last refresh timestamp display
 * 
 * Settings are persisted using DataStore and applied app-wide.
 * WorkManager scheduling is updated when auto-refresh setting changes.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    /** ViewModel for managing settings state */
    private val viewModel: SettingsViewModel by viewModels {
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkStatus.collect { isOnline ->
                    binding.textNetworkStatus.text = if (isOnline) "Online" else "Offline"
                    binding.textNetworkStatus.setTextColor(
                        if (isOnline) {
                            android.graphics.Color.parseColor("#4CAF50")
                        } else {
                            android.graphics.Color.parseColor("#F44336")
                        }
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offlineOnlyMode.collect { enabled ->
                    binding.switchOfflineMode.isChecked = enabled
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.autoRefreshEnabled.collect { enabled ->
                    binding.switchAutoRefresh.isChecked = enabled
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastRefreshTime.collect { timestamp ->
                    if (timestamp > 0) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                        binding.textLastRefresh.text = dateFormat.format(Date(timestamp))
                    } else {
                        binding.textLastRefresh.text = "Never"
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setOfflineOnlyMode(isChecked)
        }

        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoRefreshEnabled(isChecked)
            if (isChecked) {
                WorkManagerHelper.schedulePeriodicRefresh(requireContext())
            } else {
                WorkManagerHelper.cancelPeriodicRefresh(requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

