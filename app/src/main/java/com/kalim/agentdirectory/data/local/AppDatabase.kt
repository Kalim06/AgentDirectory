package com.kalim.agentdirectory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kalim.agentdirectory.data.model.Post
import com.kalim.agentdirectory.data.model.User

/**
 * Room database for local data persistence.
 * 
 * Database Configuration:
 * - Version: 1
 * - Entities: User, Post
 * - TypeConverters: Converters (for complex objects like Reactions, Address, etc.)
 * - Migration: Destructive (drops and recreates on version change)
 * 
 * Singleton pattern ensures single database instance across the app.
 */
@Database(
    entities = [User::class, Post::class],
    version = 1,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /** Data Access Object for user operations */
    abstract fun userDao(): UserDao
    
    /** Data Access Object for post operations */
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets or creates the database instance (singleton pattern).
         * 
         * @param context Application context
         * @return AppDatabase instance
         * 
         * Thread-safe implementation using synchronized block.
         * Uses application context to prevent memory leaks.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agent_directory_database"
                )
                    .fallbackToDestructiveMigration() // Drops and recreates on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

