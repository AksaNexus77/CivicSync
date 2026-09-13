package com.example.di

import com.example.data.api.CivicSyncApiService
import com.example.data.api.CivicSyncApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module providing network clients and the AI Caseworker API service.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured [OkHttpClient] with robust timeouts and basic logging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    /**
     * Provides the singleton instance of [CivicSyncApiService] backed by [CivicSyncApiServiceImpl].
     */
    @Provides
    @Singleton
    fun provideCivicSyncApiService(
        httpClient: OkHttpClient
    ): CivicSyncApiService {
        return CivicSyncApiServiceImpl(httpClient)
    }
}
