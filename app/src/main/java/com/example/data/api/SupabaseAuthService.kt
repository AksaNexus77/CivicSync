package com.example.data.api

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SupabaseAuthState {
    data object Unauthenticated : SupabaseAuthState
    data object Loading : SupabaseAuthState
    data class Authenticated(val userId: String, val email: String) : SupabaseAuthState
    data class Error(val message: String) : SupabaseAuthState
}

/**
 * Service encapsulating Supabase Authentication (Email/Password & Token)
 * and Supabase Encrypted Document Vault Storage.
 */
@Singleton
class SupabaseAuthService @Inject constructor(
    private val client: SupabaseClient,
    private val auth: Auth = client.auth,
    private val storage: Storage = client.storage
) {
    private val tag = "SupabaseAuthService"
    private val vaultBucketName = "document-vault"

    val sessionStatus: Flow<SupabaseAuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = auth.currentUserOrNull()
                SupabaseAuthState.Authenticated(
                    userId = user?.id ?: "citizen_local",
                    email = user?.email ?: "citizen@civicsync.local"
                )
            }
            else -> SupabaseAuthState.Unauthenticated
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    suspend fun signUpWithEmail(emailInput: String, passwordInput: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            auth.signUpWith(Email) {
                email = emailInput.trim()
                password = passwordInput
            }
            val userId = auth.currentUserOrNull()?.id ?: "anon_created"
            Result.success(userId)
        } catch (e: Throwable) {
            Log.e(tag, "Supabase sign-up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(emailInput: String, passwordInput: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            auth.signInWith(Email) {
                email = emailInput.trim()
                password = passwordInput
            }
            val userId = auth.currentUserOrNull()?.id ?: "authenticated_user"
            Result.success(userId)
        } catch (e: Throwable) {
            Log.e(tag, "Supabase sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Supabase sign-out failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads an encrypted evidence document to the private 'document-vault' bucket
     * partitioned by user ID to comply with RLS.
     */
    suspend fun uploadVaultDocument(
        fileName: String,
        data: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: "guest_user"
            val storagePath = "$userId/$fileName"
            val bucket = storage.from(vaultBucketName)
            bucket.upload(path = storagePath, data = data) {
                upsert = true
            }
            val publicUrl = try {
                bucket.publicUrl(storagePath)
            } catch (e: Throwable) {
                storagePath
            }
            Result.success(publicUrl)
        } catch (e: Throwable) {
            Log.e(tag, "Supabase storage upload failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteVaultDocument(storagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.from(vaultBucketName)
            bucket.delete(storagePath)
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Supabase storage deletion failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
