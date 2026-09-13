package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class CaseStatus(val labelEn: String, val labelUr: String) {
    PENDING("Pending", "زیرِ التواء"),
    IN_PROGRESS("In Progress", "زیرِ کارروائی"),
    FILED("Filed with Authority", "جمع شدہ"),
    RESOLVED("Resolved", "حل شدہ")
}

@Entity(tableName = "saved_cases")
data class SavedCaseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val situation: String,
    val province: String,
    val urgency: String,
    val status: String = CaseStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val actionPlanJson: String,
    val citizenNotes: String = ""
)
