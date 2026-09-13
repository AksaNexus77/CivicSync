package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.CivicSyncDatabase
import com.example.data.model.SupabaseCaseDto
import com.example.di.SupabaseModule
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

/**
 * AndroidX WorkManager Worker synchronizing local SQLCipher Room cases with Supabase.
 * Respects strict battery and unmetered/metered network constraints.
 */
class CaseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = CivicSyncDatabase.getDatabase(applicationContext)
            val unsyncedCases = database.savedCaseDao().getUnsyncedCases()

            if (unsyncedCases.isEmpty()) {
                Log.d("CaseSyncWorker", "No unsynced cases found.")
                return Result.success()
            }

            val client = SupabaseModule.provideSupabaseClient()
            val postgrest = client.postgrest
            val currentUserId = client.auth.currentUserOrNull()?.id ?: "local_citizen"

            for (caseEntity in unsyncedCases) {
                try {
                    val dto = SupabaseCaseDto(
                        id = caseEntity.id,
                        userId = if (caseEntity.userId.isNotBlank()) caseEntity.userId else currentUserId,
                        title = caseEntity.title,
                        situation = caseEntity.situation,
                        country = "Pakistan",
                        province = caseEntity.province,
                        urgency = caseEntity.urgency,
                        status = caseEntity.status,
                        actionPlan = caseEntity.actionPlanJson,
                        citizenNotes = caseEntity.citizenNotes
                    )
                    postgrest.from("cases").upsert(dto)
                    database.savedCaseDao().markCaseSynced(caseEntity.id)
                    Log.d("CaseSyncWorker", "Successfully synced case ${caseEntity.id} to Supabase")
                } catch (e: Throwable) {
                    Log.w("CaseSyncWorker", "Skipping cloud sync for case ${caseEntity.id}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Throwable) {
            Log.e("CaseSyncWorker", "Sync worker error: ${e.message}", e)
            Result.retry()
        }
    }
}
