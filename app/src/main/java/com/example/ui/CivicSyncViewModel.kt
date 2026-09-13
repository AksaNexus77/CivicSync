package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.CivicSyncApiService
import com.example.data.api.CivicSyncApiServiceImpl
import com.example.data.local.CaseStatus
import com.example.data.local.CivicSyncDatabase
import com.example.data.local.OfflineChecklistEntity
import com.example.data.local.SavedCaseEntity
import com.example.data.local.VaultDocumentEntity
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.CivicResource
import com.example.data.repository.CivicSyncRepository
import com.example.data.repository.CivicSyncRepositoryImpl
import com.example.util.AppLanguage
import com.example.util.SpeechAndHapticHelper
import com.example.util.Strings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Senior-level ViewModel coordinating clean architecture data flow between the AI
 * Caseworker repository, local Room persistence, and the Jetpack Compose UI.
 *
 * Implements Dependency Injection via Dagger Hilt, uses [StateFlow] exclusively for
 * reactive state representation, and leverages the [CivicSyncUiState] sealed interface
 * to drive the 3-step Intake -> Processing -> Action Plan wizard cleanly.
 */
@HiltViewModel
class CivicSyncViewModel @Inject constructor(
    private val repository: CivicSyncRepository,
    application: Application
) : AndroidViewModel(application) {

    /**
     * Fallback secondary constructor enabling seamless previewing, unit tests, and
     * runtime instantiation without requiring runtime Hilt container initialization.
     */
    constructor(application: Application) : this(
        repository = CivicSyncRepositoryImpl(
            apiService = CivicSyncApiServiceImpl(
                httpClient = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
            ),
            database = CivicSyncDatabase.getDatabase(application)
        ),
        application = application
    )

    private val speechHelper: SpeechAndHapticHelper by lazy {
        SpeechAndHapticHelper(application)
    }

    // --- StateFlow Architecture ---

    /**
     * Core wizard state flow managing Idle (Intake), Loading (Skeleton Shimmer),
     * Success (Action Plan), and Error (with Retry & Offline Fallback).
     */
    private val _uiState = MutableStateFlow<CivicSyncUiState>(CivicSyncUiState.Idle())
    val uiState: StateFlow<CivicSyncUiState> = _uiState.asStateFlow()

    /**
     * Selected application language with RTL awareness.
     */
    private val _currentLanguage = MutableStateFlow(AppLanguage.URDU)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    /**
     * Active top-level navigation destination.
     */
    private val _currentNav = MutableStateFlow(NavigationDest.HOME)
    val currentNav: StateFlow<NavigationDest> = _currentNav.asStateFlow()

    /**
     * Ephemeral toast / snackbar notification message.
     */
    private val _userToast = MutableStateFlow<String?>(null)
    val userToast: StateFlow<String?> = _userToast.asStateFlow()

    /**
     * Active case being inspected in historical detail.
     */
    private val _selectedCaseReview = MutableStateFlow<SavedCaseEntity?>(null)
    val selectedCaseReview: StateFlow<SavedCaseEntity?> = _selectedCaseReview.asStateFlow()

    // --- Room Database Observables ---

    val savedCases: StateFlow<List<SavedCaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultDocuments: StateFlow<List<VaultDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineChecklists: StateFlow<List<OfflineChecklistEntity>> = repository.offlineChecklists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPlayingTts: StateFlow<Boolean> = speechHelper.isPlayingTts

    val resources: List<CivicResource> = defaultCivicResources()

    private val loadingMessages = listOf(
        "Synthesizing institutional legal precedents & local statutes…",
        "Reviewing civil identity registries & statutory grievance channels…",
        "Drafting formal administrative representation to relevant authorities…",
        "Formulating localized citizen helpline advocacy scripts…"
    )

    private var loadingCycleJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureOfflineChecklistSeeded()
        }
    }

    // --- Localization & Language ---

    /**
     * Toggles between English and Urdu/Arabic.
     */
    fun toggleLanguage() {
        val next = when (_currentLanguage.value) {
            AppLanguage.URDU -> AppLanguage.ENGLISH
            AppLanguage.ENGLISH -> AppLanguage.URDU
            AppLanguage.ARABIC -> AppLanguage.ENGLISH
        }
        _currentLanguage.value = next
        performHapticFeedback()
    }

    /**
     * Sets a specific [AppLanguage].
     */
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    // --- Intake Form State Updates ---

    /**
     * Updates the citizen grievance narrative in the Idle state.
     */
    fun onSituationChanged(text: String) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Idle) {
            _uiState.value = current.copy(situationText = text, validationError = null)
        }
    }

    /**
     * Incorporates transcribed speech from voice input into the narrative.
     */
    fun onSpeechRecognized(transcribedText: String) {
        if (transcribedText.isNotBlank()) {
            val current = _uiState.value
            if (current is CivicSyncUiState.Idle) {
                val combined = if (current.situationText.isBlank()) {
                    transcribedText
                } else {
                    "${current.situationText} $transcribedText"
                }
                _uiState.value = current.copy(
                    situationText = combined,
                    validationError = null,
                    isRecordingVoice = false
                )
                showToast("Voice input transcribed / آواز محفوظ ہو گئی")
                performHapticFeedback()
            }
        }
    }

    /**
     * Toggles voice recording indicator state.
     */
    fun setRecordingVoice(isRecording: Boolean) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Idle) {
            _uiState.value = current.copy(isRecordingVoice = isRecording)
            if (isRecording) performHapticFeedback()
        }
    }

    /**
     * Updates the urgency classification.
     */
    fun onUrgencyChanged(level: String) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Idle) {
            _uiState.value = current.copy(urgencyLevel = level)
        }
    }

    /**
     * Updates the selected country.
     */
    fun onCountryChanged(country: String) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Idle) {
            val defaultRegion = when {
                country.contains("Pakistan", ignoreCase = true) -> "Punjab"
                country.contains("United States", ignoreCase = true) -> "California"
                country.contains("United Kingdom", ignoreCase = true) -> "London"
                else -> "Capital Region"
            }
            _uiState.value = current.copy(country = country, region = defaultRegion)
        }
    }

    /**
     * Updates the selected region / province / state.
     */
    fun onRegionChanged(region: String) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Idle) {
            _uiState.value = current.copy(region = region)
        }
    }

    /**
     * Populates intake fields from a preset sample crisis.
     */
    fun selectPreset(situation: String, urgency: String, country: String, region: String) {
        _uiState.value = CivicSyncUiState.Idle(
            situationText = situation,
            urgencyLevel = urgency,
            country = country,
            region = region,
            validationError = null
        )
        performHapticFeedback()
    }

    // --- Wizard Flow & Action Plan Generation ---

    /**
     * Initiates Action Plan synthesis via the AI caseworker repository.
     * Transitions state from [CivicSyncUiState.Idle] -> [CivicSyncUiState.Loading] -> [CivicSyncUiState.Success]
     * or [CivicSyncUiState.Error].
     */
    fun generateActionPlan() {
        val current = _uiState.value
        val (situation, urgency, country, region) = when (current) {
            is CivicSyncUiState.Idle -> Tuple4(current.situationText.trim(), current.urgencyLevel, current.country, current.region)
            is CivicSyncUiState.Error -> Tuple4(current.situationText.trim(), current.urgencyLevel, current.country, current.region)
            else -> return
        }

        if (situation.isBlank()) {
            val errMsg = Strings.get("step1_subtitle", _currentLanguage.value)
            if (current is CivicSyncUiState.Idle) {
                _uiState.value = current.copy(validationError = errMsg)
            }
            return
        }

        performHapticFeedback(60L)
        _uiState.value = CivicSyncUiState.Loading(
            situationText = situation,
            country = country,
            region = region,
            messageIndex = 0,
            message = loadingMessages.first()
        )

        startLoadingCycle()

        viewModelScope.launch {
            val docTitles = vaultDocuments.value.map { it.title }
            val langCode = _currentLanguage.value.code

            val result = repository.generateActionPlan(
                situation = situation,
                urgency = urgency,
                country = country,
                region = region,
                language = langCode,
                uploadedDocuments = docTitles
            )

            loadingCycleJob?.cancel()

            result.onSuccess { plan ->
                _uiState.value = CivicSyncUiState.Success(
                    plan = plan,
                    activeTab = PlanTab.ELIGIBILITY,
                    isOfflineFallback = false,
                    country = country,
                    region = region
                )
                performHapticFeedback(80L)
            }.onFailure { ex ->
                // Provide offline fallback plan alongside the error
                val fallbackPlan = repository.getOfflineContingencyPlan(
                    situation = situation,
                    country = country,
                    region = region,
                    uploadedDocuments = docTitles
                )

                _uiState.value = CivicSyncUiState.Error(
                    errorMessage = ex.localizedMessage ?: "Unable to contact AI caseworker engine.",
                    situationText = situation,
                    country = country,
                    region = region,
                    urgencyLevel = urgency,
                    canRetry = true,
                    fallbackPlan = fallbackPlan
                )
            }
        }
    }

    /**
     * Retries plan generation following an error state.
     */
    fun retryGeneration() {
        generateActionPlan()
    }

    /**
     * Activates the offline contingency plan immediately if the user opts out of retrying.
     */
    fun useOfflineContingencyPlan() {
        val current = _uiState.value
        if (current is CivicSyncUiState.Error && current.fallbackPlan != null) {
            _uiState.value = CivicSyncUiState.Success(
                plan = current.fallbackPlan,
                activeTab = PlanTab.ELIGIBILITY,
                isOfflineFallback = true,
                country = current.country,
                region = current.region
            )
            showToast("Loaded offline contingency guide / آف لائن گائیڈ لوڈ ہو گئی")
            performHapticFeedback(80L)
        }
    }

    /**
     * Resets the wizard back to the initial Intake step.
     */
    fun resetToIntake() {
        val current = _uiState.value
        val (situation, urgency, country, region) = when (current) {
            is CivicSyncUiState.Idle -> Tuple4(current.situationText, current.urgencyLevel, current.country, current.region)
            is CivicSyncUiState.Success -> Tuple4("", "Immediate Crisis", current.country, current.region)
            is CivicSyncUiState.Error -> Tuple4(current.situationText, current.urgencyLevel, current.country, current.region)
            is CivicSyncUiState.Loading -> Tuple4(current.situationText, "Immediate Crisis", current.country, current.region)
        }

        _uiState.value = CivicSyncUiState.Idle(
            situationText = situation,
            urgencyLevel = urgency,
            country = country,
            region = region
        )
        _selectedCaseReview.value = null
        stopSpeech()
    }

    /**
     * Selects an active tab in the Action Plan view.
     */
    fun selectTab(tab: PlanTab) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Success) {
            _uiState.value = current.copy(activeTab = tab)
            stopSpeech()
        }
    }

    /**
     * Toggles the completion status of an Action Plan checklist item.
     */
    fun toggleChecklistItem(itemId: String) {
        val current = _uiState.value
        if (current is CivicSyncUiState.Success) {
            val updated = current.plan.actionChecklist.map { item ->
                if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
            }
            _uiState.value = current.copy(plan = current.plan.copy(actionChecklist = updated))
            performHapticFeedback(40L)
        }
    }

    // --- Case Saving & Local Management ---

    /**
     * Persists the active Action Plan to the local Room database as a tracked citizen case.
     */
    fun saveCurrentPlanAsCase(titleOverride: String? = null, notes: String = "") {
        val current = _uiState.value
        if (current !is CivicSyncUiState.Success) return

        val plan = current.plan
        val title = titleOverride?.ifBlank { null }
            ?: if (current.region.isNotBlank()) "Grievance Case (${current.region})" else "Citizen Hardship Case"

        val planJson = try {
            JSONObject().apply {
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
                        put("id", ch.id)
                        put("task", ch.task)
                        put("timeline", ch.timeline)
                        put("category", ch.category)
                        put("isCompleted", ch.isCompleted)
                    })
                }
                put("actionChecklist", checkArray)
                put("draftLetter", plan.draftLetter)
                put("advocacyScript", plan.advocacyScript)
                put("disclaimer", plan.disclaimer)
            }.toString()
        } catch (e: Exception) {
            ""
        }

        viewModelScope.launch {
            val entity = SavedCaseEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                situation = "Case in ${current.region}, ${current.country}",
                province = current.region,
                urgency = "Saved Case",
                status = CaseStatus.PENDING.name,
                createdAt = System.currentTimeMillis(),
                actionPlanJson = planJson,
                citizenNotes = notes
            )
            repository.saveCase(entity)
            showToast(Strings.get("case_saved_success", _currentLanguage.value))
            performHapticFeedback(80L)
        }
    }

    /**
     * Loads and reviews a previously saved case record into the Action Plan view.
     */
    fun viewSavedCase(case: SavedCaseEntity) {
        try {
            val obj = JSONObject(case.actionPlanJson)
            val eligList = mutableListOf<com.example.data.model.EligibilityItem>()
            val eligArr = obj.optJSONArray("eligibilitySummary")
            if (eligArr != null) {
                for (i in 0 until eligArr.length()) {
                    val item = eligArr.optJSONObject(i) ?: continue
                    eligList.add(
                        com.example.data.model.EligibilityItem(
                            benefit = item.optString("benefit", "Benefit"),
                            reason = item.optString("reason", "Reason"),
                            urgency = item.optString("urgency", "Urgent")
                        )
                    )
                }
            }

            val checkList = mutableListOf<ChecklistItem>()
            val checkArr = obj.optJSONArray("actionChecklist")
            if (checkArr != null) {
                for (i in 0 until checkArr.length()) {
                    val item = checkArr.optJSONObject(i) ?: continue
                    checkList.add(
                        ChecklistItem(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            task = item.optString("task", "Task"),
                            timeline = item.optString("timeline", "Immediate"),
                            category = item.optString("category", "General"),
                            isCompleted = item.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            val plan = CivicActionPlan(
                eligibilitySummary = eligList,
                actionChecklist = checkList,
                draftLetter = obj.optString("draftLetter", ""),
                advocacyScript = obj.optString("advocacyScript", ""),
                disclaimer = obj.optString("disclaimer", "")
            )

            _selectedCaseReview.value = case
            _uiState.value = CivicSyncUiState.Success(
                plan = plan,
                activeTab = PlanTab.ELIGIBILITY,
                isOfflineFallback = false,
                country = "Pakistan",
                region = case.province
            )
            _currentNav.value = NavigationDest.HOME
            performHapticFeedback()
        } catch (e: Exception) {
            showToast("Failed loading case details")
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

    // --- Document Vault ---

    fun addVaultDocument(title: String, docType: String, uriString: String? = null, description: String = "") {
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

    fun toggleOfflineChecklist(id: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleOfflineChecklist(id, completed)
            performHapticFeedback(40L)
        }
    }

    /**
     * Permanently purges all user cases, vault documents, and resets offline checklist state.
     * Complies with Google Play User Data & Account Deletion Policy.
     */
    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            _selectedCaseReview.value = null
            _uiState.value = CivicSyncUiState.Idle()
            showToast("All personal records & vault data permanently purged")
            performHapticFeedback(100L)
        }
    }

    // --- Navigation & Audio ---

    fun navigateTo(dest: NavigationDest) {
        _currentNav.value = dest
        stopSpeech()
    }

    fun speakAdvocacyScript(scriptText: String) {
        speechHelper.speak(scriptText, _currentLanguage.value)
        performHapticFeedback()
    }

    fun stopSpeech() {
        speechHelper.stopSpeech()
    }

    fun performHapticFeedback(pattern: Long = 40L) {
        speechHelper.performHapticFeedback(pattern)
    }

    fun showToast(message: String) {
        _userToast.value = message
        viewModelScope.launch {
            delay(3500)
            _userToast.value = null
        }
    }

    private fun startLoadingCycle() {
        loadingCycleJob?.cancel()
        loadingCycleJob = viewModelScope.launch {
            var index = 0
            while (true) {
                delay(2200)
                index = (index + 1) % loadingMessages.size
                val cur = _uiState.value
                if (cur is CivicSyncUiState.Loading) {
                    _uiState.value = cur.copy(
                        messageIndex = index,
                        message = loadingMessages[index]
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.shutdown()
        loadingCycleJob?.cancel()
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}

fun defaultCivicResources(): List<CivicResource> = listOf(
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
        description = "Statutory constitutional body resolving public complaints against federal agencies (NADRA, BISP, EOBI, WAPDA) free of cost.",
        contact = "1055",
        badge = "Free Redressal"
    ),
    CivicResource(
        title = "NADRA Citizen Facilitation Helpline",
        category = "Civil Registration & CNIC",
        description = "Direct helpline for inquiries regarding blocked CNICs, Family Registration Certificates (FRC), and biometric verification boards.",
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
        title = "Legal Services Corporation & Civil Legal Aid",
        category = "Pro Bono Representation (USA/Global)",
        description = "Public interest legal defense providing emergency eviction prevention and administrative appeal advocacy.",
        contact = "211",
        badge = "Legal Aid"
    ),
    CivicResource(
        title = "Citizens Advice Bureau & Ombudsman",
        category = "Statutory Welfare Counseling (UK/Global)",
        description = "Independent confidential guidance on Universal Credit, council housing rights, and unfair government decisions.",
        contact = "0800 144 8848",
        badge = "Counsel"
    )
)
