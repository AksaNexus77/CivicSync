package com.example.data.repository

import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocumentEntity
import com.example.data.model.CivicActionPlan
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining access to both remote AI caseworker services
 * and local offline database persistence (Saved Cases, Vault Documents, Offline Guides).
 */
interface CivicSyncRepository {

    /**
     * Observable stream of all citizen cases saved locally.
     */
    val allCases: Flow<List<SavedCaseEntity>>

    /**
     * Observable stream of all citizen identity and proof documents in the vault.
     */
    val allDocuments: Flow<List<VaultDocumentEntity>>

    /**
     * Observable stream of offline statutory procedural checklists.
     */
    val offlineChecklists: Flow<List<OfflineChecklistEntity>>

    /**
     * Calls the AI caseworker engine to generate a localized legal aid and welfare action plan.
     */
    suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        country: String,
        region: String,
        language: String,
        uploadedDocuments: List<String> = emptyList()
    ): Result<CivicActionPlan>

    /**
     * Generates a deterministic offline contingency plan when network is unavailable.
     */
    fun getOfflineContingencyPlan(
        situation: String,
        country: String,
        region: String,
        uploadedDocuments: List<String> = emptyList()
    ): CivicActionPlan

    /**
     * Persists an active case to local Room storage.
     */
    suspend fun saveCase(caseEntity: SavedCaseEntity)

    /**
     * Updates the status of an existing case record.
     */
    suspend fun updateCaseStatus(caseId: String, newStatus: String)

    /**
     * Removes a case from the local database.
     */
    suspend fun deleteCase(caseId: String)

    /**
     * Stores a document reference in the local secure vault.
     */
    suspend fun saveDocument(document: VaultDocumentEntity)

    /**
     * Removes a document reference from the local vault.
     */
    suspend fun deleteDocument(docId: String)

    /**
     * Toggles completion status of an offline checklist step.
     */
    suspend fun toggleOfflineChecklist(id: String, isCompleted: Boolean)

    /**
     * Seeds statutory checklists if database is newly initialized.
     */
    suspend fun ensureOfflineChecklistSeeded()

    /**
     * Permanently purges all user data: saved cases, vault documents, and resets checklists.
     * Complies with Google Play User Data Deletion and privacy regulations.
     */
    suspend fun clearAllUserData()
}
