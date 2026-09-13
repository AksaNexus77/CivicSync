package com.example.data.repository

import com.example.data.api.CivicSyncApiService
import com.example.data.local.CivicSyncDatabase
import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocumentEntity
import com.example.data.model.CivicActionPlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [CivicSyncRepository] coordinating local Room persistence
 * and AI-driven caseworker services with offline contingencies.
 */
@Singleton
class CivicSyncRepositoryImpl @Inject constructor(
    private val apiService: CivicSyncApiService,
    private val database: CivicSyncDatabase
) : CivicSyncRepository {

    override val allCases: Flow<List<SavedCaseEntity>> =
        database.savedCaseDao().getAllCases()

    override val allDocuments: Flow<List<VaultDocumentEntity>> =
        database.vaultDocumentDao().getAllDocuments()

    override val offlineChecklists: Flow<List<OfflineChecklistEntity>> =
        database.offlineChecklistDao().getAllChecklists()

    override suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        country: String,
        region: String,
        language: String,
        uploadedDocuments: List<String>
    ): Result<CivicActionPlan> {
        return apiService.generateActionPlan(
            situation = situation,
            urgency = urgency,
            country = country,
            region = region,
            language = language,
            uploadedDocuments = uploadedDocuments
        )
    }

    override fun getOfflineContingencyPlan(
        situation: String,
        country: String,
        region: String,
        uploadedDocuments: List<String>
    ): CivicActionPlan {
        return apiService.getOfflineContingencyPlan(
            situation = situation,
            country = country,
            region = region,
            uploadedDocuments = uploadedDocuments
        )
    }

    override suspend fun saveCase(caseEntity: SavedCaseEntity) {
        database.savedCaseDao().insertCase(caseEntity)
    }

    override suspend fun updateCaseStatus(caseId: String, newStatus: String) {
        database.savedCaseDao().updateCaseStatus(caseId, newStatus)
    }

    override suspend fun deleteCase(caseId: String) {
        database.savedCaseDao().deleteCase(caseId)
    }

    override suspend fun saveDocument(document: VaultDocumentEntity) {
        database.vaultDocumentDao().insertDocument(document)
    }

    override suspend fun deleteDocument(docId: String) {
        database.vaultDocumentDao().deleteDocument(docId)
    }

    override suspend fun toggleOfflineChecklist(id: String, isCompleted: Boolean) {
        database.offlineChecklistDao().updateCompletion(id, isCompleted)
    }

    override suspend fun ensureOfflineChecklistSeeded() {
        if (database.offlineChecklistDao().count() == 0) {
            database.offlineChecklistDao().insertAll(CivicSyncDatabase.seedOfflineChecklist())
        }
    }
}
