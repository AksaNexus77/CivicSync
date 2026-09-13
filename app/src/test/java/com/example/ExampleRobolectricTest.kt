package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.CivicSyncApiServiceImpl
import com.example.data.model.CivicActionPlan
import com.example.ui.CivicSyncUiState
import com.example.ui.CivicSyncViewModel
import com.example.ui.PlanTab
import com.example.util.AppLanguage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Enterprise Robolectric unit tests validating Clean Architecture,
 * sealed UiState transitions, dynamic API parameters, and localized contingency plans.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches application identity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CivicSync Global", appName)
    }

    @Test
    fun `test offline contingency plan generation for Pakistan jurisdiction`() {
        val service = CivicSyncApiServiceImpl(OkHttpClient())
        val plan = service.getOfflineContingencyPlan(
            situation = "Mother's biometric verification failed at BISP office and health card was rejected at hospital",
            country = "Pakistan",
            region = "Punjab"
        )

        assertTrue("Eligibility summary should not be empty", plan.eligibilitySummary.isNotEmpty())
        assertTrue("Checklist should not be empty", plan.actionChecklist.isNotEmpty())
        assertTrue("Letter should have citizen placeholder", plan.draftLetter.contains("[Citizen Full Name]"))
        assertTrue("Advocacy script should cite CNIC", plan.advocacyScript.contains("CNIC"))
        assertTrue("Statutory disclaimer present", plan.disclaimer.contains("Statutory Notice"))
    }

    @Test
    fun `test offline contingency plan generation for USA jurisdiction`() {
        val service = CivicSyncApiServiceImpl(OkHttpClient())
        val plan = service.getOfflineContingencyPlan(
            situation = "Emergency food stamps (SNAP) cutoff unexpectedly",
            country = "United States",
            region = "California"
        )

        assertTrue(plan.eligibilitySummary.any { it.benefit.contains("SNAP") })
        assertTrue(plan.actionChecklist.any { it.task.contains("SNAP") })
        assertTrue(plan.advocacyScript.contains("California"))
    }

    @Test
    fun `test strict kotlinx serialization with civic welfare schema`() {
        val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        val rawJson = """
            {
              "eligibilitySummary": [
                {"benefit": "BISP Benazir Kafalat", "reason": "PMT score under eligibility threshold", "urgency": "Immediate Crisis"},
                {"benefit": "Universal Healthcare Coverage", "reason": "Indoor surgical admission", "urgency": "Immediate Crisis"}
              ],
              "actionChecklist": [
                {"id": "test-id-1", "task": "Visit municipal civil registry", "timeline": "Immediate (Day 1)", "category": "Civil Identity", "isCompleted": false},
                {"id": "test-id-2", "task": "File administrative petition", "timeline": "Within 48 hours", "category": "Ombudsman", "isCompleted": true}
              ],
              "draftLetter": "# Formal Representation\n\nI, [Citizen Full Name] respectfully petition...",
              "advocacyScript": "Hello, I am calling regarding my urgent case...",
              "disclaimer": "This is AI-generated guidance, not licensed legal advice."
            }
        """.trimIndent()

        val parsed = jsonParser.decodeFromString<CivicActionPlan>(rawJson)
        assertEquals(2, parsed.eligibilitySummary.size)
        assertEquals("BISP Benazir Kafalat", parsed.eligibilitySummary[0].benefit)
        assertEquals(2, parsed.actionChecklist.size)
        assertEquals("test-id-1", parsed.actionChecklist[0].id)
        assertEquals(false, parsed.actionChecklist[0].isCompleted)
        assertEquals(true, parsed.actionChecklist[1].isCompleted)
    }

    @Test
    fun `test viewmodel stateflow and sealed uistate transitions`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CivicSyncViewModel(app)

        // Initial state should be Idle
        val initialState = vm.uiState.value
        assertTrue("Initial state must be CivicSyncUiState.Idle", initialState is CivicSyncUiState.Idle)
        val idleState = initialState as CivicSyncUiState.Idle
        assertEquals("Punjab", idleState.region)
        assertEquals("Pakistan", idleState.country)
        assertEquals(AppLanguage.URDU, vm.currentLanguage.value)

        // Test multilingual toggle
        vm.toggleLanguage()
        assertEquals(AppLanguage.ENGLISH, vm.currentLanguage.value)

        // Test form updates
        vm.onSituationChanged("Biometric verification halted")
        val updatedState = vm.uiState.value as CivicSyncUiState.Idle
        assertEquals("Biometric verification halted", updatedState.situationText)

        // Test country & region change
        vm.onCountryChanged("United States")
        val stateAfterCountry = vm.uiState.value as CivicSyncUiState.Idle
        assertEquals("United States", stateAfterCountry.country)
        assertEquals("California", stateAfterCountry.region)

        // Test preset selection
        vm.selectPreset(
            situation = "Eviction notice dispute",
            urgency = "This Week",
            country = "United Kingdom",
            region = "London"
        )
        val presetState = vm.uiState.value as CivicSyncUiState.Idle
        assertEquals("Eviction notice dispute", presetState.situationText)
        assertEquals("United Kingdom", presetState.country)
        assertEquals("London", presetState.region)
    }
}
