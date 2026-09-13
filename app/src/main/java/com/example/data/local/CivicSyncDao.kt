package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCaseDao {
    @Query("SELECT * FROM saved_cases ORDER BY createdAt DESC")
    fun getAllCases(): Flow<List<SavedCaseEntity>>

    @Query("SELECT * FROM saved_cases WHERE id = :caseId LIMIT 1")
    suspend fun getCaseById(caseId: String): SavedCaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: SavedCaseEntity)

    @Update
    suspend fun updateCase(caseEntity: SavedCaseEntity)

    @Query("UPDATE saved_cases SET status = :newStatus WHERE id = :caseId")
    suspend fun updateCaseStatus(caseId: String, newStatus: String)

    @Query("DELETE FROM saved_cases WHERE id = :caseId")
    suspend fun deleteCase(caseId: String)
}

@Dao
interface VaultDocumentDao {
    @Query("SELECT * FROM vault_documents ORDER BY uploadedAt DESC")
    fun getAllDocuments(): Flow<List<VaultDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: VaultDocumentEntity)

    @Query("DELETE FROM vault_documents WHERE id = :docId")
    suspend fun deleteDocument(docId: String)
}

@Dao
interface OfflineChecklistDao {
    @Query("SELECT * FROM offline_checklists ORDER BY id ASC")
    fun getAllChecklists(): Flow<List<OfflineChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OfflineChecklistEntity>)

    @Query("UPDATE offline_checklists SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompletion(id: String, completed: Boolean)

    @Query("SELECT COUNT(*) FROM offline_checklists")
    suspend fun count(): Int
}
