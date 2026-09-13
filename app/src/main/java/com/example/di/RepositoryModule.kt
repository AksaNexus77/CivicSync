package com.example.di

import android.content.Context
import com.example.data.api.CivicSyncApiService
import com.example.data.api.SupabaseAuthService
import com.example.data.local.CivicSyncDatabase
import com.example.data.repository.CaseRepository
import com.example.data.repository.CaseRepositoryImpl
import com.example.data.repository.CivicSyncRepository
import com.example.data.repository.CivicSyncRepositoryImpl
import com.example.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module binding the database and repository layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides the Room database singleton instance.
     */
    @Provides
    @Singleton
    fun provideCivicSyncDatabase(
        @ApplicationContext context: Context
    ): CivicSyncDatabase {
        return CivicSyncDatabase.getDatabase(context)
    }

    /**
     * Provides the domain [CivicSyncRepository] instance backed by [CivicSyncRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideCivicSyncRepository(
        apiService: CivicSyncApiService,
        database: CivicSyncDatabase
    ): CivicSyncRepository {
        return CivicSyncRepositoryImpl(apiService, database)
    }

    /**
     * Provides the offline-first [CaseRepository] instance backed by [CaseRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideCaseRepository(
        database: CivicSyncDatabase,
        authService: SupabaseAuthService,
        postgrest: Postgrest,
        syncManager: SyncManager,
        @ApplicationContext context: Context
    ): CaseRepository {
        return CaseRepositoryImpl(database, authService, postgrest, syncManager, context)
    }
}
