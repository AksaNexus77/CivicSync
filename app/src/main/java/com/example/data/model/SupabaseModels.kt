package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseCaseDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val situation: String,
    val country: String = "Pakistan",
    val province: String,
    val urgency: String,
    val status: String,
    @SerialName("action_plan") val actionPlan: String,
    @SerialName("citizen_notes") val citizenNotes: String = ""
)

@Serializable
data class SupabaseDocumentDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("case_id") val caseId: String? = null,
    val title: String,
    @SerialName("doc_type") val docType: String,
    @SerialName("storage_path") val storagePath: String,
    @SerialName("mime_type") val mimeType: String = "image/jpeg",
    val description: String = ""
)
