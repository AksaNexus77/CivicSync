package com.example.ui

import com.example.data.model.CivicActionPlan

/**
 * Navigation destinations for top-level app navigation.
 */
enum class NavigationDest {
    HOME,
    ACTIVE_CASES,
    DOCUMENT_VAULT,
    OFFLINE_GUIDES,
    RESOURCES,
    SETTINGS
}

/**
 * Tab selectors within the Action Plan screen.
 */
enum class PlanTab {
    ELIGIBILITY,
    CHECKLIST,
    DRAFT_LETTER,
    ADVOCACY_SCRIPT
}

/**
 * Sealed interface representing the state transitions of the CivicSync application.
 * Manages the 3-step wizard workflow (Intake -> Processing/Loading -> Action Plan) cleanly,
 * handling idle input, shimmering skeletons during processing, successful plan presentations,
 * and resilient error states with offline fallback affordances.
 */
sealed interface CivicSyncUiState {

    /**
     * Initial Intake step: The user enters grievance narratives, selects urgency,
     * country, and administrative region.
     *
     * @property situationText Raw text describing the citizen's hardship or denial.
     * @property urgencyLevel Urgency of the situation (Immediate Crisis, This Week, Planning Ahead).
     * @property country Selected country jurisdiction (default Pakistan, supporting USA, UK, Global).
     * @property region Selected administrative province/state.
     * @property isRecordingVoice True if speech recognition is currently active.
     * @property validationError Local validation error if input was submitted empty.
     */
    data class Idle(
        val situationText: String = "",
        val urgencyLevel: String = "Immediate Crisis",
        val country: String = "Pakistan",
        val region: String = "Punjab",
        val isRecordingVoice: Boolean = false,
        val validationError: String? = null
    ) : CivicSyncUiState

    /**
     * Processing step: Represents active AI generation with animated skeleton shimmer
     * and dynamic progress messaging.
     *
     * @property situationText The input situation text being processed.
     * @property country The target country.
     * @property region The target region.
     * @property messageIndex Index of the current rotating loading message.
     * @property message Current user-friendly loading status message.
     */
    data class Loading(
        val situationText: String = "",
        val country: String = "Pakistan",
        val region: String = "Punjab",
        val messageIndex: Int = 0,
        val message: String = "Synthesizing statutory frameworks & local programs…"
    ) : CivicSyncUiState

    /**
     * Success step: The AI has successfully synthesized the citizen's customized Action Plan,
     * complete with eligibility assessment, checklist steps, legal letter, and helpline script.
     *
     * @property plan The resulting [CivicActionPlan].
     * @property activeTab The currently selected tab in the Action Plan view.
     * @property isOfflineFallback True if generated via the deterministic offline contingency engine.
     * @property country The target country of the plan.
     * @property region The target region of the plan.
     * @property userToast Ephemeral feedback message (e.g. copied to clipboard).
     */
    data class Success(
        val plan: CivicActionPlan,
        val activeTab: PlanTab = PlanTab.ELIGIBILITY,
        val isOfflineFallback: Boolean = false,
        val country: String = "Pakistan",
        val region: String = "Punjab",
        val userToast: String? = null
    ) : CivicSyncUiState

    /**
     * Error step: Represents an unexpected failure during synthesis or network outage,
     * offering an immediate retry trigger and access to offline contingency checklists.
     *
     * @property errorMessage The human-readable error description.
     * @property situationText Preserved input situation so user does not lose their progress.
     * @property country Preserved country selection.
     * @property region Preserved region selection.
     * @property urgencyLevel Preserved urgency selection.
     * @property canRetry Whether the action can be retried.
     * @property fallbackPlan Offline contingency plan available if user wishes to bypass error.
     */
    data class Error(
        val errorMessage: String,
        val situationText: String = "",
        val country: String = "Pakistan",
        val region: String = "Punjab",
        val urgencyLevel: String = "Immediate Crisis",
        val canRetry: Boolean = true,
        val fallbackPlan: CivicActionPlan? = null
    ) : CivicSyncUiState
}
