package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiCaseworkerService
import com.example.data.model.CaseRecord
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.CivicResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class WizardStep {
    INTAKE,
    PROCESSING,
    ACTION_PLAN
}

enum class NavigationDest {
    HOME,
    ACTIVE_CASES,
    RESOURCES,
    SETTINGS
}

enum class PlanTab {
    ELIGIBILITY,
    CHECKLIST,
    DRAFT_LETTER,
    ADVOCACY_SCRIPT
}

data class CivicSyncUiState(
    val currentStep: WizardStep = WizardStep.INTAKE,
    val currentNav: NavigationDest = NavigationDest.HOME,
    val activeTab: PlanTab = PlanTab.ELIGIBILITY,
    val situationText: String = "",
    val urgencyLevel: String = "Immediate Crisis",
    val locationText: String = "California",
    val isLoading: Boolean = false,
    val loadingMessageIndex: Int = 0,
    val currentPlan: CivicActionPlan? = null,
    val errorMessage: String? = null,
    val activeCases: List<CaseRecord> = emptyList(),
    val resources: List<CivicResource> = defaultResources()
)

fun defaultResources(): List<CivicResource> = listOf(
    CivicResource(
        title = "National Legal Aid & Defender Association",
        category = "Legal Representation",
        description = "Network of public defense and civil legal aid advocates across all 50 states for low-income individuals.",
        contact = "1-800-LAW-AID1",
        badge = "Free Counsel"
    ),
    CivicResource(
        title = "National Eviction Defense Hotline & 2-1-1",
        category = "Housing & Emergency Stay",
        description = "Connects immediately with certified housing counselors, emergency tenant protection funds, and mediation.",
        contact = "Dial 2-1-1 (24/7 Toll-Free)",
        badge = "24/7 Hotline"
    ),
    CivicResource(
        title = "USDA SNAP Emergency Nutrition Helpline",
        category = "Food Security",
        description = "Expedited emergency food stamp processing for households with less than $100 liquid assets or zero income.",
        contact = "1-800-221-5689",
        badge = "Nutrition"
    ),
    CivicResource(
        title = "LawHelp.org Free Civil Legal Navigator",
        category = "Bureaucracy Navigation",
        description = "Comprehensive legal rights manuals, statutory appeal deadlines, and pro bono attorney directories by county.",
        contact = "www.lawhelp.org",
        badge = "Directory"
    ),
    CivicResource(
        title = "National Consumer Law Center (NCLC)",
        category = "Consumer & Debt Defense",
        description = "Protections against unlawful collections, garnishment exemptions, and medical debt appeal templates.",
        contact = "www.nclc.org",
        badge = "Rights Guide"
    )
)

class CivicSyncViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CivicSyncUiState())
    val uiState: StateFlow<CivicSyncUiState> = _uiState.asStateFlow()

    private var loadingCycleJob: Job? = null

    val loadingMessages = listOf(
        "Analyzing local policies...",
        "Drafting legal documents...",
        "Compiling your action plan...",
        "Verifying statutory deadlines and appeal rights..."
    )

    fun onSituationChanged(text: String) {
        _uiState.update { it.copy(situationText = text, errorMessage = null) }
    }

    fun onUrgencyChanged(urgency: String) {
        _uiState.update { it.copy(urgencyLevel = urgency) }
    }

    fun onLocationChanged(location: String) {
        _uiState.update { it.copy(locationText = location) }
    }

    fun selectNav(dest: NavigationDest) {
        _uiState.update { it.copy(currentNav = dest) }
    }

    fun selectTab(tab: PlanTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun populatePreset(situation: String, urgency: String, location: String) {
        _uiState.update {
            it.copy(
                situationText = situation,
                urgencyLevel = urgency,
                locationText = location,
                errorMessage = null
            )
        }
    }

    fun toggleChecklistItem(itemId: String) {
        _uiState.update { state ->
            val plan = state.currentPlan ?: return@update state
            val updatedChecklist = plan.actionChecklist.map { item ->
                if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
            }
            state.copy(currentPlan = plan.copy(actionChecklist = updatedChecklist))
        }
    }

    fun resetToIntake() {
        _uiState.update {
            it.copy(
                currentStep = WizardStep.INTAKE,
                currentNav = NavigationDest.HOME,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun loadCase(record: CaseRecord) {
        _uiState.update {
            it.copy(
                currentStep = WizardStep.ACTION_PLAN,
                currentNav = NavigationDest.HOME,
                activeTab = PlanTab.ELIGIBILITY,
                situationText = record.situation,
                urgencyLevel = record.urgency,
                locationText = record.location,
                currentPlan = record.plan,
                errorMessage = null
            )
        }
    }

    fun deleteCase(caseId: String) {
        _uiState.update { state ->
            state.copy(activeCases = state.activeCases.filter { it.id != caseId })
        }
    }

    fun generateActionPlan() {
        val currentSituation = _uiState.value.situationText.trim()
        if (currentSituation.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please describe your situation to build your action plan.") }
            return
        }

        // Before calling the API, set isLoading to true and clear previous results
        _uiState.update {
            it.copy(
                isLoading = true,
                currentStep = WizardStep.PROCESSING,
                currentPlan = null,
                errorMessage = null,
                loadingMessageIndex = 0
            )
        }

        // Start cycling through the loading messages
        loadingCycleJob?.cancel()
        loadingCycleJob = viewModelScope.launch {
            var idx = 0
            while (_uiState.value.isLoading) {
                delay(1200)
                idx = (idx + 1) % loadingMessages.size
                _uiState.update { it.copy(loadingMessageIndex = idx) }
            }
        }

        viewModelScope.launch {
            val result = GeminiCaseworkerService.generateActionPlan(
                situation = currentSituation,
                urgency = _uiState.value.urgencyLevel,
                location = _uiState.value.locationText
            )

            loadingCycleJob?.cancel()

            result.fold(
                onSuccess = { plan ->
                    val nowFormatted = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date())
                    val newRecord = CaseRecord(
                        id = UUID.randomUUID().toString(),
                        dateFormatted = nowFormatted,
                        situation = currentSituation,
                        urgency = _uiState.value.urgencyLevel,
                        location = _uiState.value.locationText,
                        plan = plan
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currentStep = WizardStep.ACTION_PLAN,
                            activeTab = PlanTab.ELIGIBILITY,
                            currentPlan = plan,
                            activeCases = listOf(newRecord) + state.activeCases,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currentStep = WizardStep.INTAKE,
                            errorMessage = error.message ?: "Our AI caseworker is experiencing high volume. Please try again in a moment."
                        )
                    }
                }
            )
        }
    }
}
