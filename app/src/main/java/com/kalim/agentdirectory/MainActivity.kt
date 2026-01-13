package com.kalim.agentdirectory

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.kalim.agentdirectory.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import android.view.Menu
import android.view.MenuItem

/**
 * Main Activity that hosts all fragments using Navigation Component.
 * 
 * Responsibilities:
 * - Sets up Navigation Component with toolbar integration
 * - Handles menu inflation and settings navigation
 * - Manages back button behavior for child fragments
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Navigation Controller from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set the MaterialToolbar as the support action bar for proper integration
        setSupportActionBar(binding.toolbar)

        // Configure AppBar: Define top-level destinations (no back button shown on these screens)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.agentDirectoryFragment,  // Home screen - no back button
                R.id.settingsFragment         // Settings screen - no back button
            ),
            null // No drawer layout
        )

        // Connect Navigation Component with ActionBar for automatic title updates
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Setup toolbar to handle back button navigation automatically
        // This enables back button on child fragments (like AgentProfileFragment)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
    
    /**
     * Inflate the options menu (Settings button) in the toolbar.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    /**
     * Handle menu item clicks.
     * Currently handles Settings menu item navigation.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Navigate to Settings screen when Settings icon is clicked
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}