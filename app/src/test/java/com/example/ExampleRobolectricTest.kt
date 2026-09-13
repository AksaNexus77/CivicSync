package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiCaseworkerService
import com.example.ui.CivicSyncViewModel
import com.example.ui.PlanTab
import com.example.ui.WizardStep
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CivicSync AI", appName)
  }

  @Test
  fun `test fallback plan generation`() {
    val plan = GeminiCaseworkerService.buildEmpatheticSynthesizedPlan(
      situation = "Landlord gave me 3 days notice due to sudden job loss",
      urgency = "Immediate Crisis",
      location = "California"
    )
    assertTrue(plan.eligibilitySummary.isNotEmpty())
    assertTrue(plan.actionChecklist.isNotEmpty())
    assertTrue(plan.draftLetter.contains("FORMAL NOTICE"))
    assertTrue(plan.advocacyScript.contains("Hello, my name is"))
  }

  @Test
  fun `test json parser with raw schema`() {
    val rawJson = """
      {
        "eligibilitySummary": [
          {"benefit": "SNAP Emergency", "reason": "Zero income", "urgency": "Immediate Crisis"}
        ],
        "actionChecklist": [
          {"task": "File Notice", "timeline": "Within 24h", "category": "Legal Defense"}
        ],
        "draftLetter": "To Whom It May Concern...",
        "advocacyScript": "I am calling to request review...",
        "disclaimer": "This is AI-generated guidance, not licensed legal advice."
      }
    """.trimIndent()

    val parsed = GeminiCaseworkerService.parseJsonToActionPlan(JSONObject(rawJson))
    assertEquals(1, parsed.eligibilitySummary.size)
    assertEquals("SNAP Emergency", parsed.eligibilitySummary[0].benefit)
    assertEquals(1, parsed.actionChecklist.size)
    assertEquals("File Notice", parsed.actionChecklist[0].task)
    assertEquals("This is AI-generated guidance, not licensed legal advice.", parsed.disclaimer)
  }

  @Test
  fun `test viewmodel state transitions`() {
    val vm = CivicSyncViewModel()
    assertEquals(WizardStep.INTAKE, vm.uiState.value.currentStep)

    vm.onSituationChanged("Test eviction notice received")
    assertEquals("Test eviction notice received", vm.uiState.value.situationText)

    vm.selectTab(PlanTab.CHECKLIST)
    assertEquals(PlanTab.CHECKLIST, vm.uiState.value.activeTab)
  }
}

