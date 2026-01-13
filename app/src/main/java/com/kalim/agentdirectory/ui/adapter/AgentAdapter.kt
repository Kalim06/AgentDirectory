package com.kalim.agentdirectory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kalim.agentdirectory.data.model.User
import com.kalim.agentdirectory.databinding.ItemAgentBinding

/**
 * RecyclerView adapter for displaying agent list.
 * 
 * Features:
 * - Uses ListAdapter with DiffUtil for efficient list updates
 * - Handles item clicks via callback
 * - Loads images with Glide (caching and error handling)
 * 
 * Performance:
 * - DiffUtil automatically calculates differences between old and new lists
 * - Only updates changed items, not entire list
 * - View recycling for memory efficiency
 */
class AgentAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, AgentAdapter.AgentViewHolder>(AgentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        val binding = ItemAgentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AgentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for agent list items.
     * Binds user data to views and handles click events.
     */
    class AgentViewHolder(
        private val binding: ItemAgentBinding,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds user data to the view holder.
         * 
         * @param user The user data to display
         */
        fun bind(user: User) {
            binding.apply {
                textName.text = user.fullName
                textEmail.text = user.email
                textPhone.text = user.phone
                textCompany.text = user.company?.name ?: "N/A"

                // Load image with Glide (automatic caching and error handling)
                Glide.with(root.context)
                    .load(user.image)
                    .circleCrop() // Transform to circular avatar
                    .placeholder(android.R.drawable.ic_menu_gallery) // Show while loading
                    .error(android.R.drawable.ic_menu_gallery) // Show on error
                    .into(imageAvatar)

                root.setOnClickListener {
                    onItemClick(user)
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     * Compares items to determine what changed between list updates.
     */
    class AgentDiffCallback : DiffUtil.ItemCallback<User>() {
        /**
         * Checks if two items represent the same user (by ID).
         */
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Checks if the contents of two users are the same.
         */
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

