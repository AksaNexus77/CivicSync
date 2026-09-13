package com.example.di

import com.example.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module providing Supabase client instances.
 * Provides SupabaseClient, Postgrest, Auth, and Storage for enterprise legal casework sync.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val DEFAULT_SUPABASE_URL = "https://civicsync.supabase.co"
    private const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_civicsync"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val supabaseUrl = try {
            val field = BuildConfig::class.java.getField("SUPABASE_URL")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() } ?: DEFAULT_SUPABASE_URL
        } catch (e: Throwable) {
            DEFAULT_SUPABASE_URL
        }

        val supabaseAnonKey = try {
            val field = BuildConfig::class.java.getField("SUPABASE_ANON_KEY")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() } ?: DEFAULT_ANON_KEY
        } catch (e: Throwable) {
            DEFAULT_ANON_KEY
        }

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage {
        return client.storage
    }
}
