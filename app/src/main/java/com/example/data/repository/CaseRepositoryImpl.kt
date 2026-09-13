package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.SupabaseAuthService
import com.example.data.local.CivicSyncDatabase
import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocumentEntity
import com.example.data.model.SupabaseCaseDto
import com.example.data.model.SupabaseDocumentDto
import com.example.data.sync.SyncManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaseRepositoryImpl @Inject constructor(
    private val database: CivicSyncDatabase,
    private val authService: SupabaseAuthService,
    private val postgrest: Postgrest,
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
) : CaseRepository {

    private val tag = "CaseRepository"

    override fun getAllCases(): Flow<List<SavedCaseEntity>> {
        return database.savedCaseDao().getAllCases()
    }

    override suspend fun getCaseById(caseId: String): SavedCaseEntity? = withContext(Dispatchers.IO) {
        database.savedCaseDao().getCaseById(caseId)
    }

    override suspend fun saveCase(caseEntity: SavedCaseEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = authService.getCurrentUserId() ?: ""
            val entityToSave = caseEntity.copy(
                userId = if (caseEntity.userId.isNotBlank()) caseEntity.userId else currentUserId,
                isSynced = false
            )
            // 1. Save to encrypted Room first (Offline-First guarantee)
            database.savedCaseDao().insertCase(entityToSave)

            // 2. Trigger background sync via WorkManager
            syncManager.triggerImmediateSync()

            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to save case: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCaseStatus(caseId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.savedCaseDao().updateCaseStatus(caseId, newStatus)
            syncManager.triggerImmediateSync()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to update case status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCase(caseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.savedCaseDao().deleteCase(caseId)
            try {
                postgrest.from("cases").delete {
                    filter {
                        eq("id", caseId)
                    }
                }
            } catch (e: Throwable) {
                Log.w(tag, "Could not delete case remotely from Supabase: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to delete case: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncCasesWithSupabase(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            syncManager.triggerImmediateSync()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override fun getAllDocuments(): Flow<List<VaultDocumentEntity>> {
        return database.vaultDocumentDao().getAllDocuments()
    }

    override suspend fun saveDocument(
        document: VaultDocumentEntity,
        fileBytes: ByteArray?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = authService.getCurrentUserId() ?: ""
            var uploadedRemoteUrl: String? = null

            // Upload bytes to Supabase storage if provided and connected
            if (fileBytes != null && fileBytes.isNotEmpty()) {
                val uploadResult = authService.uploadVaultDocument(
                    fileName = "${document.id}_${document.title.replace(" ", "_")}",
                    data = fileBytes,
                    mimeType = document.mimeType
                )
                uploadedRemoteUrl = uploadResult.getOrNull()
            }

            val entityToSave = document.copy(
                userId = currentUserId,
                remoteUrl = uploadedRemoteUrl ?: document.remoteUrl,
                isSynced = uploadedRemoteUrl != null
            )

            // Save to encrypted Room
            database.vaultDocumentDao().insertDocument(entityToSave)

            // Insert metadata into Supabase documents table if remote upload succeeded
            if (uploadedRemoteUrl != null) {
                try {
                    val dto = SupabaseDocumentDto(
                        id = entityToSave.id,
                        userId = currentUserId,
                        caseId = entityToSave.caseId,
                        title = entityToSave.title,
                        docType = entityToSave.docType,
                        storagePath = uploadedRemoteUrl,
                        mimeType = entityToSave.mimeType,
                        description = entityToSave.description
                    )
                    postgrest.from("documents").upsert(dto)
                } catch (e: Throwable) {
                    Log.w(tag, "Failed to write document metadata to Supabase: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to save document: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(docId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.vaultDocumentDao().deleteDocument(docId)
            try {
                postgrest.from("documents").delete {
                    filter {
                        eq("id", docId)
                    }
                }
            } catch (e: Throwable) {
                Log.w(tag, "Could not delete document remotely from Supabase: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to delete document: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getAllOfflineChecklists(): Flow<List<OfflineChecklistEntity>> {
        return database.offlineChecklistDao().getAllChecklists()
    }

    override suspend fun toggleChecklistCompletion(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        database.offlineChecklistDao().updateCompletion(id, isCompleted)
    }

    override suspend fun clearAllUserData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Purge all Room database tables
            database.savedCaseDao().deleteAllCases()
            database.vaultDocumentDao().deleteAllDocuments()
            database.offlineChecklistDao().resetAllChecklistCompletions()

            // 2. Purge Supabase user tables if logged in
            val currentUserId = authService.getCurrentUserId()
            if (!currentUserId.isNullOrBlank()) {
                try {
                    postgrest.from("cases").delete {
                        filter {
                            eq("user_id", currentUserId)
                        }
                    }
                    postgrest.from("documents").delete {
                        filter {
                            eq("user_id", currentUserId)
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(tag, "Failed to purge remote Supabase data: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Failed to clear all user data: ${e.message}", e)
            Result.failure(e)
        }
    }
}
