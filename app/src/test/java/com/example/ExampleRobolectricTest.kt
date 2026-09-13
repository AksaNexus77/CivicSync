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
  fun `test fallback plan generation for Pakistan welfare context`() {
    val plan = GeminiCaseworkerService.buildEmpatheticSynthesizedPlan(
      situation = "Meri walida ki BISP Kafalat biometric verify nahi ho rahi aur Sehat Card hospital mein reject ho gaya",
      urgency = "Immediate Crisis",
      location = "Punjab"
    )
    assertTrue(plan.eligibilitySummary.isNotEmpty())
    assertTrue(plan.actionChecklist.isNotEmpty())
    assertTrue(plan.draftLetter.contains("[Name]"))
    assertTrue(plan.draftLetter.contains("[CNIC Number]"))
    assertTrue(plan.draftLetter.contains("[Address]"))
    assertTrue(plan.advocacyScript.contains("[CNIC Number]"))
    assertTrue(plan.disclaimer.contains("This is AI-generated guidance, not licensed legal advice"))
  }

  @Test
  fun `test json parser with Pakistani welfare schema`() {
    val rawJson = """
      {
        "eligibilitySummary": [
          {"benefit": "BISP Benazir Kafalat", "reason": "PMT score under eligibility threshold", "urgency": "Immediate Crisis"},
          {"benefit": "Sehat Sahulat Card", "reason": "Empaneled indoor hospitalization", "urgency": "Immediate Crisis"}
        ],
        "actionChecklist": [
          {"task": "Visit NADRA center with CNIC", "timeline": "Immediate (Day 1)", "category": "Civil Identity"},
          {"task": "File complaint with Wafaqi Mohtasib", "timeline": "Within 48 hours", "category": "Ombudsman"}
        ],
        "draftLetter": "# Grievance to Deputy Commissioner\n\nI, [Name], CNIC: [CNIC Number], resident of [Address]...",
        "advocacyScript": "Assalam-o-Alaikum, mera naam [Name] hai, CNIC [CNIC Number]...",
        "disclaimer": "This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office."
      }
    """.trimIndent()

    val parsed = GeminiCaseworkerService.parseJsonToActionPlan(JSONObject(rawJson))
    assertEquals(2, parsed.eligibilitySummary.size)
    assertEquals("BISP Benazir Kafalat", parsed.eligibilitySummary[0].benefit)
    assertEquals(2, parsed.actionChecklist.size)
    assertEquals("Visit NADRA center with CNIC", parsed.actionChecklist[0].task)
    assertEquals("This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office.", parsed.disclaimer)
  }

  @Test
  fun `test viewmodel state transitions and defaults`() {
    val vm = CivicSyncViewModel()
    assertEquals(WizardStep.INTAKE, vm.uiState.value.currentStep)
    assertEquals("Punjab", vm.uiState.value.locationText)

    vm.onSituationChanged("BISP biometric failure report")
    assertEquals("BISP biometric failure report", vm.uiState.value.situationText)

    vm.selectTab(PlanTab.CHECKLIST)
    assertEquals(PlanTab.CHECKLIST, vm.uiState.value.activeTab)
  }
}
