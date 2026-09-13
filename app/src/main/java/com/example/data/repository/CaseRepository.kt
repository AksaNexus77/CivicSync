package com.example.data.repository

import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Offline-First repository interface for Citizen Casework and Vault Records.
 * Guarantees zero data loss by committing to local SQLCipher-encrypted Room storage
 * before synchronizing with remote Supabase services.
 */
interface CaseRepository {

    // --- Active Cases ---
    fun getAllCases(): Flow<List<SavedCaseEntity>>

    suspend fun getCaseById(caseId: String): SavedCaseEntity?

    suspend fun saveCase(caseEntity: SavedCaseEntity): Result<Unit>

    suspend fun updateCaseStatus(caseId: String, newStatus: String): Result<Unit>

    suspend fun deleteCase(caseId: String): Result<Unit>

    suspend fun syncCasesWithSupabase(): Result<Unit>

    // --- Encrypted Document Vault ---
    fun getAllDocuments(): Flow<List<VaultDocumentEntity>>

    suspend fun saveDocument(
        document: VaultDocumentEntity,
        fileBytes: ByteArray? = null
    ): Result<Unit>

    suspend fun deleteDocument(docId: String): Result<Unit>

    // --- Offline Checklists ---
    fun getAllOfflineChecklists(): Flow<List<OfflineChecklistEntity>>

    suspend fun toggleChecklistCompletion(id: String, isCompleted: Boolean)

    // --- Global Data Deletion (Play Store Compliance) ---
    suspend fun clearAllUserData(): Result<Unit>
}
