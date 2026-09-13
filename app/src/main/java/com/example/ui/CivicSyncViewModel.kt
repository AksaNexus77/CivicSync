package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiCaseworkerService
import com.example.data.local.CaseStatus
import com.example.data.local.CivicSyncDatabase
import com.example.data.local.CivicSyncRepository
import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocType
import com.example.data.local.VaultDocumentEntity
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.CivicResource
import com.example.util.AppLanguage
import com.example.util.SpeechAndHapticHelper
import com.example.util.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
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
    DOCUMENT_VAULT,
    OFFLINE_GUIDES,
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
    val language: AppLanguage = AppLanguage.URDU,
    val currentStep: WizardStep = WizardStep.INTAKE,
    val currentNav: NavigationDest = NavigationDest.HOME,
    val activeTab: PlanTab = PlanTab.ELIGIBILITY,
    val situationText: String = "",
    val urgencyLevel: String = "Immediate Crisis",
    val locationText: String = "Punjab",
    val isLoading: Boolean = false,
    val loadingMessageIndex: Int = 0,
    val currentPlan: CivicActionPlan? = null,
    val errorMessage: String? = null,
    val userToast: String? = null,
    val isRecordingVoice: Boolean = false,
    val selectedCaseReview: SavedCaseEntity? = null,
    val resources: List<CivicResource> = defaultPakistaniResources()
)

fun defaultPakistaniResources(): List<CivicResource> = listOf(
    CivicResource(
        title = "Benazir Income Support Programme (BISP)",
        category = "Social Protection & Kafalat",
        description = "Official national helpline for BISP Kafalat quarterly stipend, dynamic registry, and biometric dispute resolution.",
        contact = "0800-26477",
        badge = "Toll-Free"
    ),
    CivicResource(
        title = "Wafaqi Mohtasib (Federal Ombudsman Secretariat)",
        category = "Administrative Grievance Tribunal",
        description = "Statutory constitutional body resolving public complaints against federal agencies (NADRA, BISP, EOBI, WAPDA, SNGPL) free of cost.",
        contact = "1055",
        badge = "Free Redressal"
    ),
    CivicResource(
        title = "NADRA Citizen Facilitation Helpline",
        category = "Civil Registration & CNIC",
        description = "Direct helpline for inquiries regarding blocked CNICs, Family Registration Certificates (FRC), CRC/B-Forms, and biometric verification boards.",
        contact = "1777",
        badge = "Helpline"
    ),
    CivicResource(
        title = "Sehat Sahulat Program / Sehat Card Plus",
        category = "Universal Health Coverage",
        description = "Free indoor medical care, surgical treatment, and emergency hospitalization coverage for citizens across Pakistan.",
        contact = "0800-09009",
        badge = "Healthcare"
    ),
    CivicResource(
        title = "Pakistan Bait-ul-Mal (PBM) Emergency Assistance",
        category = "Mustahiqeen & Zakat Relief",
        description = "Financial assistance, life-saving medical grants, artificial limbs, and education stipends for impoverished individuals.",
        contact = "0800-66666",
        badge = "Emergency Aid"
    ),
    CivicResource(
        title = "Legal Aid Society & Sindh Legal Advisory Call Center",
        category = "Pro Bono Legal Representation",
        description = "Free civil, family, and criminal legal advice provided by licensed high court advocates and legal aid caseworkers.",
        contact = "0800-70806",
        badge = "Free Counsel"
    )
)

class CivicSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CivicSyncRepository by lazy {
        val db = CivicSyncDatabase.getDatabase(application)
        CivicSyncRepository(db)
    }

    private val speechHelper: SpeechAndHapticHelper by lazy {
        SpeechAndHapticHelper(application)
    }

    private val _uiState = MutableStateFlow(CivicSyncUiState())
    val uiState: StateFlow<CivicSyncUiState> = _uiState.asStateFlow()

    // Room Database Observables
    val savedCases: StateFlow<List<SavedCaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultDocuments: StateFlow<List<VaultDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineChecklists: StateFlow<List<OfflineChecklistEntity>> = repository.offlineChecklists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPlayingTts: StateFlow<Boolean> = speechHelper.isPlayingTts

    val loadingMessages = listOf(
        "Synthesizing institutional legal precedents...",
        "Reviewing NADRA, BISP & statutory grievance channels...",
        "Drafting formal representation to relevant authorities...",
        "Preparing Urdu & English citizen advocacy scripts..."
    )

    private var loadingCycleJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureOfflineChecklistSeeded()
        }
    }

    // Language Toggle
    fun toggleLanguage() {
        val newLang = if (_uiState.value.language == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
        _uiState.update { it.copy(language = newLang) }
        performHapticFeedback()
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang) }
    }

    // Input handlers
    fun onSituationChanged(text: String) {
        _uiState.update { it.copy(situationText = text, errorMessage = null) }
    }

    fun onSpeechRecognized(transcribedText: String) {
        if (transcribedText.isNotBlank()) {
            val current = _uiState.value.situationText
            val newText = if (current.isBlank()) transcribedText else "$current $transcribedText"
            _uiState.update { it.copy(situationText = newText, errorMessage = null, isRecordingVoice = false) }
            showToast("Speech recognized / آواز محفوظ ہو گئی")
            performHapticFeedback()
        }
    }

    fun setRecordingVoice(isRecording: Boolean) {
        _uiState.update { it.copy(isRecordingVoice = isRecording) }
        if (isRecording) performHapticFeedback()
    }

    fun onUrgencyChanged(level: String) {
        _uiState.update { it.copy(urgencyLevel = level) }
    }

    fun onLocationChanged(location: String) {
        _uiState.update { it.copy(locationText = location) }
    }

    fun selectPreset(situation: String, urgency: String, location: String) {
        _uiState.update {
            it.copy(
                situationText = situation,
                urgencyLevel = urgency,
                locationText = location,
                errorMessage = null
            )
        }
        performHapticFeedback()
    }

    fun populatePreset(situation: String, urgency: String, location: String) =
        selectPreset(situation, urgency, location)

    fun navigateTo(dest: NavigationDest) {
        _uiState.update { it.copy(currentNav = dest) }
        stopSpeech()
    }

    fun selectNav(dest: NavigationDest) = navigateTo(dest)

    fun resetToIntake() = startNewIntake()

    fun loadSavedCase(case: SavedCaseEntity) = viewSavedCase(case)

    fun selectTab(tab: PlanTab) {
        _uiState.update { it.copy(activeTab = tab) }
        stopSpeech()
    }

    // Generation workflow
    fun generateActionPlan() {
        val state = _uiState.value
        val situation = state.situationText.trim()
        if (situation.isBlank()) {
            val errMsg = Strings.get("step1_subtitle", state.language)
            _uiState.update { it.copy(errorMessage = errMsg) }
            return
        }

        performHapticFeedback(60L)
        _uiState.update {
            it.copy(
                isLoading = true,
                currentStep = WizardStep.PROCESSING,
                loadingMessageIndex = 0,
                errorMessage = null
            )
        }

        startLoadingMessagesCycle()

        viewModelScope.launch {
            // Include document titles from vault in prompt
            val docTitles = vaultDocuments.value.map { it.title }

            val result = GeminiCaseworkerService.generateActionPlan(
                situation = situation,
                urgency = state.urgencyLevel,
                location = state.locationText,
                uploadedDocuments = docTitles
            )

            loadingCycleJob?.cancel()

            result.onSuccess { plan ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = WizardStep.ACTION_PLAN,
                        currentPlan = plan,
                        activeTab = PlanTab.ELIGIBILITY,
                        errorMessage = null
                    )
                }
                performHapticFeedback(80L)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = WizardStep.INTAKE,
                        errorMessage = err.message ?: Strings.get("error_general", state.language)
                    )
                }
            }
        }
    }

    fun retryGeneration() {
        generateActionPlan()
    }

    fun startNewIntake() {
        _uiState.update {
            it.copy(
                currentStep = WizardStep.INTAKE,
                situationText = "",
                currentPlan = null,
                errorMessage = null
            )
        }
        stopSpeech()
    }

    // Save Case to Room Database
    fun saveCurrentPlanAsCase(titleOverride: String? = null, notes: String = "") {
        val state = _uiState.value
        val plan = state.currentPlan ?: return

        val title = titleOverride?.ifBlank { null }
            ?: if (state.situationText.length > 50) state.situationText.take(50) + "..." else state.situationText.ifBlank { "Citizen Welfare Case" }

        val planJson = try {
            val obj = JSONObject().apply {
                val eligArray = org.json.JSONArray()
                plan.eligibilitySummary.forEach { el ->
                    eligArray.put(JSONObject().apply {
                        put("benefit", el.benefit)
                        put("reason", el.reason)
                        put("urgency", el.urgency)
                    })
                }
                put("eligibilitySummary", eligArray)

                val checkArray = org.json.JSONArray()
                plan.actionChecklist.forEach { ch ->
                    checkArray.put(JSONObject().apply {
                        put("task", ch.task)
                        put("timeline", ch.timeline)
                        put("category", ch.category)
                    })
                }
                put("actionChecklist", checkArray)

                put("draftLetter", plan.draftLetter)
                put("advocacyScript", plan.advocacyScript)
                put("disclaimer", plan.disclaimer)
            }
            obj.toString()
        } catch (e: Exception) {
            ""
        }

        viewModelScope.launch {
            val caseEntity = SavedCaseEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                situation = state.situationText,
                province = state.locationText,
                urgency = state.urgencyLevel,
                status = CaseStatus.PENDING.name,
                createdAt = System.currentTimeMillis(),
                actionPlanJson = planJson,
                citizenNotes = notes
            )
            repository.saveCase(caseEntity)
            showToast(Strings.get("case_saved_success", state.language))
            performHapticFeedback(80L)
        }
    }

    fun updateCaseStatus(caseId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateCaseStatus(caseId, newStatus)
            showToast("Status updated / حیثیت تبدیل ہو گئی")
            performHapticFeedback()
        }
    }

    fun deleteCase(caseId: String) {
        viewModelScope.launch {
            repository.deleteCase(caseId)
            showToast("Case removed / کیس خارج کر دیا گیا")
            performHapticFeedback()
        }
    }

    fun viewSavedCase(case: SavedCaseEntity) {
        try {
            val planObj = JSONObject(case.actionPlanJson)
            val plan = GeminiCaseworkerService.parseJsonToActionPlan(planObj)
            _uiState.update {
                it.copy(
                    currentStep = WizardStep.ACTION_PLAN,
                    currentPlan = plan,
                    selectedCaseReview = case,
                    situationText = case.situation,
                    locationText = case.province,
                    urgencyLevel = case.urgency,
                    currentNav = NavigationDest.HOME
                )
            }
            performHapticFeedback()
        } catch (e: Exception) {
            showToast("Could not load case details")
        }
    }

    // Document Vault Management
    fun addVaultDocument(
        title: String,
        docType: String,
        uriString: String? = null,
        description: String = ""
    ) {
        viewModelScope.launch {
            val doc = VaultDocumentEntity(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Citizen Document" },
                docType = docType,
                uriString = uriString,
                description = description,
                uploadedAt = System.currentTimeMillis()
            )
            repository.saveDocument(doc)
            showToast("Document secured in Vault / دستاویز محفوظ ہو گئی")
            performHapticFeedback(60L)
        }
    }

    fun deleteVaultDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
            showToast("Document deleted")
            performHapticFeedback()
        }
    }

    // Checklist toggles
    fun toggleChecklistItem(itemId: String) {
        val currentPlan = _uiState.value.currentPlan ?: return
        val updatedChecklist = currentPlan.actionChecklist.map { item ->
            if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
        }
        _uiState.update {
            it.copy(currentPlan = currentPlan.copy(actionChecklist = updatedChecklist))
        }
        performHapticFeedback(40L)
    }

    fun toggleOfflineChecklist(id: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleOfflineChecklist(id, completed)
            performHapticFeedback(40L)
        }
    }

    // Audio & Haptics
    fun speakAdvocacyScript(scriptText: String) {
        speechHelper.speak(scriptText, _uiState.value.language)
        performHapticFeedback()
    }

    fun stopSpeech() {
        speechHelper.stopSpeech()
    }

    fun performHapticFeedback(pattern: Long = 40L) {
        speechHelper.performHapticFeedback(pattern)
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(userToast = message) }
        viewModelScope.launch {
            delay(3500)
            _uiState.update { it.copy(userToast = null) }
        }
    }

    private fun startLoadingMessagesCycle() {
        loadingCycleJob?.cancel()
        loadingCycleJob = viewModelScope.launch {
            var index = 0
            while (true) {
                _uiState.update { it.copy(loadingMessageIndex = index) }
                delay(2200)
                index = (index + 1) % 4
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.shutdown()
        loadingCycleJob?.cancel()
    }
}
