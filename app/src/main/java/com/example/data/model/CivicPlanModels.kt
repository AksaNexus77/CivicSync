package com.example.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EligibilityItem(
    val benefit: String,
    val reason: String,
    val urgency: String
)

@Serializable
data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val task: String,
    val timeline: String,
    val category: String,
    val isCompleted: Boolean = false
)

@Serializable
data class HelplineItem(
    val title: String,
    val contact: String,
    val category: String = "Legal Aid & Welfare",
    val description: String = ""
)

@Serializable
data class CivicActionPlan(
    val eligibilitySummary: List<EligibilityItem> = emptyList(),
    val actionChecklist: List<ChecklistItem> = emptyList(),
    val draftLetter: String = "",
    val advocacyScript: String = "",
    val localHelplines: List<HelplineItem> = emptyList(),
    val disclaimer: String = "This is AI-generated guidance, not licensed legal advice. Please consult a qualified legal professional or local authority."
)

data class CaseRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val situation: String,
    val urgency: String,
    val location: String,
    val plan: CivicActionPlan
)

data class CivicResource(
    val title: String,
    val category: String,
    val description: String,
    val contact: String,
    val badge: String
)
