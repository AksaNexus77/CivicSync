package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class VaultDocType(val labelEn: String, val labelUr: String) {
    CNIC_FRONT("CNIC (Front)", "شناختی کارڈ (سامنے)"),
    CNIC_BACK("CNIC (Back)", "شناختی کارڈ (پیچھے)"),
    FRC("Family Registration Certificate (FRC)", "خاندانی رجسٹریشن سرٹیفکیٹ (FRC)"),
    BISP_SLIP("BISP Survey Slip / SMS", "بے نظیر کفالت پرچی / ایس ایم ایس"),
    SEHAT_CARD("Sehat Card / Health Slip", "صحت کارڈ / پرچی"),
    RENT_AGREEMENT("Rent / Tenancy Agreement", "کرایہ نامہ"),
    UTILITY_BILL("Electricity / Gas Bill", "بجلی یا گیس کا بل"),
    DEATH_CERTIFICATE("Death Certificate (Late Spouse/Parent)", "ڈیتھ سرٹیفکیٹ"),
    DISABILITY_CERTIFICATE("Disability Certificate", "معذوری سرٹیفکیٹ"),
    OTHER("Other Supporting Document", "دیگر دستاویز")
}

@Entity(tableName = "vault_documents")
data class VaultDocumentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val caseId: String? = null,
    val title: String,
    val docType: String,
    val uriString: String? = null,
    val remoteUrl: String? = null,
    val mimeType: String = "image/jpeg",
    val description: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "offline_checklists")
data class OfflineChecklistEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val categoryUr: String,
    val title: String,
    val titleUr: String,
    val description: String,
    val descriptionUr: String,
    val helpline: String,
    val isCompleted: Boolean = false
)
