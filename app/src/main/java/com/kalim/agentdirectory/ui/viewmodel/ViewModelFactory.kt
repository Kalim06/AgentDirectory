package com.kalim.agentdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kalim.agentdirectory.data.repository.AgentRepository
import com.kalim.agentdirectory.util.NetworkMonitor
import com.kalim.agentdirectory.util.SettingsManager

class ViewModelFactory(
    private val repository: AgentRepository,
    private val settingsManager: SettingsManager,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentDirectoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentDirectoryViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AgentProfileViewModelFactory(
    private val repository: AgentRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentProfileViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

