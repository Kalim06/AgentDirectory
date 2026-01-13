package com.kalim.agentdirectory.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module providing Retrofit API service configuration.
 * 
 * This object sets up:
 * - OkHttp client with timeouts and logging
 * - Retrofit instance with Gson converter
 * - API service interface implementation
 * 
 * Configuration optimized for low-connectivity environments:
 * - 30-second timeouts to handle slow networks
 * - Retry on connection failure for resilience
 * - HTTP logging for debugging (can be disabled in production)
 */
object NetworkModule {
    private const val BASE_URL = "https://dummyjson.com/"

    /**
     * HTTP logging interceptor for debugging network requests.
     * Logs request/response bodies at BODY level.
     * Note: Should be disabled or set to NONE in production builds.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp client configuration optimized for field operations:
     * - 30-second timeouts (connect, read, write) for slow networks
     * - Automatic retry on connection failure
     * - HTTP logging for debugging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Wait up to 30s to establish connection
        .readTimeout(30, TimeUnit.SECONDS)   // Wait up to 30s to read response
        .writeTimeout(30, TimeUnit.SECONDS)  // Wait up to 30s to write request
        .retryOnConnectionFailure(true)      // Automatically retry failed connections
        .build()

    /**
     * Retrofit instance configured with:
     * - Base URL for dummyjson.com API
     * - OkHttp client with custom configuration
     * - Gson converter for JSON parsing
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * API service instance for making network requests.
     * Provides type-safe API calls defined in ApiService interface.
     */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

