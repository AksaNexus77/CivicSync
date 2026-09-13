package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CivicSyncRepository(private val database: CivicSyncDatabase) {

    val allCases: Flow<List<SavedCaseEntity>> = database.savedCaseDao().getAllCases()
    val allDocuments: Flow<List<VaultDocumentEntity>> = database.vaultDocumentDao().getAllDocuments()
    val offlineChecklists: Flow<List<OfflineChecklistEntity>> = database.offlineChecklistDao().getAllChecklists()

    suspend fun saveCase(caseEntity: SavedCaseEntity) {
        database.savedCaseDao().insertCase(caseEntity)
    }

    suspend fun updateCaseStatus(caseId: String, newStatus: String) {
        database.savedCaseDao().updateCaseStatus(caseId, newStatus)
    }

    suspend fun deleteCase(caseId: String) {
        database.savedCaseDao().deleteCase(caseId)
    }

    suspend fun saveDocument(document: VaultDocumentEntity) {
        database.vaultDocumentDao().insertDocument(document)
    }

    suspend fun deleteDocument(docId: String) {
        database.vaultDocumentDao().deleteDocument(docId)
    }

    suspend fun toggleOfflineChecklist(id: String, isCompleted: Boolean) {
        database.offlineChecklistDao().updateCompletion(id, isCompleted)
    }

    suspend fun ensureOfflineChecklistSeeded() {
        if (database.offlineChecklistDao().count() == 0) {
            database.offlineChecklistDao().insertAll(CivicSyncDatabase.seedOfflineChecklist())
        }
    }
}
