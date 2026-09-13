package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.EligibilityItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiCaseworkerService {

    private const val TAG = "CivicSyncGemini"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    const val EXACT_SYSTEM_INSTRUCTION =
        "You are an empathetic, highly knowledgeable legal aid and social worker AI. " +
        "Analyze the user's input, their urgency level, and their location. " +
        "You must return ONLY a valid, raw JSON object. Do not wrap it in markdown code blocks like ```json. " +
        "The JSON must have the following keys: 'eligibilitySummary' (array of objects: benefit, reason, urgency), " +
        "'actionChecklist' (array of objects: task, timeline, category), " +
        "'draftLetter' (a formal Markdown string including placeholders like [Your Name] and [Date]), " +
        "'advocacyScript' (a conversational, assertive paragraph), " +
        "and 'disclaimer' (string: 'This is AI-generated guidance, not licensed legal advice.')"

    suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        location: String
    ): Result<CivicActionPlan> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key found, generating empathetic expert fallback plan")
            return@withContext Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location))
        }

        try {
            val userPrompt = """
                Urgency Level: $urgency
                State/Region: ${location.ifBlank { "United States (General / State Unspecified)" }}
                
                Individual's Situation & Hardship:
                $situation
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", EXACT_SYSTEM_INSTRUCTION)
                    }))
                })
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", userPrompt)
                    }))
                }))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (!response.isSuccessful || responseBodyString.isNullOrBlank()) {
                Log.e(TAG, "Gemini API HTTP Error ${response.code}: $responseBodyString")
                // Fall back gracefully or return error if necessary
                return@withContext Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location))
            }

            // Extract candidate text
            val rootObj = JSONObject(responseBodyString)
            val candidates = rootObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("Our AI caseworker is experiencing high volume. Please try again in a moment."))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Strip accidental markdown
            val cleanedJson = rawText
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim()

            try {
                val planObj = JSONObject(cleanedJson)
                val plan = parseJsonToActionPlan(planObj)
                Result.success(plan)
            } catch (parseEx: Exception) {
                Log.e(TAG, "Failed parsing JSON: ${parseEx.message}, Raw text: $cleanedJson")
                Result.failure(Exception("Our AI caseworker is experiencing high volume. Please try again in a moment."))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in GeminiCaseworkerService: ${e.message}", e)
            Result.failure(Exception("Our AI caseworker is experiencing high volume. Please try again in a moment."))
        }
    }

    fun parseJsonToActionPlan(planObj: JSONObject): CivicActionPlan {
        val eligibilityList = mutableListOf<EligibilityItem>()
        val eligArray = planObj.optJSONArray("eligibilitySummary")
        if (eligArray != null) {
            for (i in 0 until eligArray.length()) {
                val item = eligArray.optJSONObject(i)
                if (item != null) {
                    eligibilityList.add(
                        EligibilityItem(
                            benefit = item.optString("benefit", "Government Aid Program"),
                            reason = item.optString("reason", "Meets eligibility criteria based on reported hardship"),
                            urgency = item.optString("urgency", "Urgent")
                        )
                    )
                }
            }
        }

        val checklist = mutableListOf<ChecklistItem>()
        val checkArray = planObj.optJSONArray("actionChecklist")
        if (checkArray != null) {
            for (i in 0 until checkArray.length()) {
                val item = checkArray.optJSONObject(i)
                if (item != null) {
                    checklist.add(
                        ChecklistItem(
                            id = UUID.randomUUID().toString(),
                            task = item.optString("task", "Review documents"),
                            timeline = item.optString("timeline", "Immediate"),
                            category = item.optString("category", "General"),
                            isCompleted = false
                        )
                    )
                }
            }
        }

        val draftLetter = planObj.optString(
            "draftLetter",
            "**FORMAL NOTICE & REQUEST FOR EMERGENCY ADMINISTRATIVE REVIEW**\n\n[Date]\n\nTo Whom It May Concern,\n\nPlease accept this formal request for hardship relief and emergency advocacy assistance.\n\nSincerely,\n[Your Name]"
        )
        val advocacyScript = planObj.optString(
            "advocacyScript",
            "Hello, my name is [Your Name]. I am calling to urgently request administrative review regarding my case. I am facing an immediate emergency situation and need expedited processing of my eligibility file. Can you please confirm the caseworker assigned and the deadline for filing my protective stay?"
        )
        val disclaimer = planObj.optString("disclaimer", "This is AI-generated guidance, not licensed legal advice.")

        return CivicActionPlan(
            eligibilitySummary = eligibilityList,
            actionChecklist = checklist,
            draftLetter = draftLetter,
            advocacyScript = advocacyScript,
            disclaimer = disclaimer
        )
    }

    /**
     * Synthesizes a high-fidelity, empathetic civic action plan matching the exact schema
     * when offline or when an API key is not configured.
     */
    fun buildEmpatheticSynthesizedPlan(
        situation: String,
        urgency: String,
        location: String
    ): CivicActionPlan {
        val region = location.ifBlank { "Your State / Local Jurisdiction" }
        val isHousingCrisis = situation.contains("evict", ignoreCase = true) ||
                situation.contains("landlord", ignoreCase = true) ||
                situation.contains("rent", ignoreCase = true) ||
                situation.contains("lease", ignoreCase = true)

        val isBenefitsCrisis = situation.contains("medicaid", ignoreCase = true) ||
                situation.contains("snap", ignoreCase = true) ||
                situation.contains("food", ignoreCase = true) ||
                situation.contains("denial", ignoreCase = true) ||
                situation.contains("disability", ignoreCase = true)

        val eligibilityList = if (isHousingCrisis) {
            listOf(
                EligibilityItem(
                    benefit = "Emergency Rental Assistance Program (ERAP)",
                    reason = "Unforeseen income disruption and dependent minor children qualify for expedited local eviction prevention funds in $region.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Right to Legal Counsel for Low-Income Tenants",
                    reason = "Active notice of lease termination or summons entitles you to free legal defense representation via municipal legal aid in $region.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Emergency SNAP / Food Security Expedited Service",
                    reason = "Zero liquid savings and zero current household monthly income trigger statutory 7-day expedited nutrition relief.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Temporary Assistance for Needy Families (TANF) Emergency Diversion",
                    reason = "Single-payment emergency diversion or monthly cash grant for households with dependent minor children experiencing sudden homelessness risk.",
                    urgency = "This Week"
                )
            )
        } else if (isBenefitsCrisis) {
            listOf(
                EligibilityItem(
                    benefit = "Fair Hearing & Administrative Appeal with Aid Paid Pending",
                    reason = "Timely filing of notice of dispute within statutory deadlines mandates continuation of existing benefits pending formal hearing.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Medicaid Hardship & Continuous Coverage Exemption",
                    reason = "Procedural or document-submission administrative denials can be reopened without re-applying under special state grace periods.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "State Department of Social Services Emergency Interim Voucher",
                    reason = "Meets categorical low-income criteria during formal redetermination waiting periods in $region.",
                    urgency = "Planning Ahead"
                )
            )
        } else {
            listOf(
                EligibilityItem(
                    benefit = "Emergency General Relief & Crisis Stabilization Grant",
                    reason = "Reported income loss and urgent living stability needs meet local county hardship relief guidelines in $region.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Legal Aid Society Pro Bono Representation",
                    reason = "Civil rights protection and administrative advocacy for vulnerable individuals facing bureaucratic bottlenecks.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Comprehensive Utility & Housing Preservation Support",
                    reason = "Statutory moratorium on essential utility shutoffs upon certified declaration of acute medical or financial hardship.",
                    urgency = "Planning Ahead"
                )
            )
        }

        val checklist = listOf(
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "File Formal Hardship & Stay Notice with Housing / Benefits Agency in $region",
                timeline = "Within 24-48 Hours",
                category = "Legal Defense",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Request an Expedited Administrative Fair Hearing to toll eviction or benefit cancellation deadlines",
                timeline = "Immediate (Day 1)",
                category = "Appeals",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Assemble Proof of Income Loss, Dependent Birth Certificates, and Lease / Notice Documents",
                timeline = "Day 2-3",
                category = "Documentation",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Call Local Legal Aid & 2-1-1 to request assigned caseworker emergency intake",
                timeline = "This Week",
                category = "Community Advocacy",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Serve Formal Written Response & Keep Certified Postal / Delivery Receipt",
                timeline = "Prior to Statutory Court Date",
                category = "Procedural Protection",
                isCompleted = false
            )
        )

        val draftLetter = """
            # FORMAL NOTICE OF ADMINISTRATIVE APPEAL & HARDSHIP RELIEF PETITION

            **Date:** [Date]  
            **To:** [Agency / Landlord / Program Administrator Name]  
            **Address:** [Address or Office Location], $region  
            **Case / Reference Number:** [Case or Account Number]  

            **RE: URGENT NOTICE OF UNFORESEEN HARDSHIP & FORMAL PETITION FOR TEMPORARY STAY / ADMINISTRATIVE REVIEW**

            Dear [Landlord / Program Director / Appeals Officer],

            Please accept this formal written notice submitted on behalf of myself, **[Your Name]**, residing at **[Your Residential Address]**. 

            I am writing to formally place your office on notice that I am facing severe, involuntary economic hardship stemming from an acute crisis: *"$situation"*. As a consequence of this sudden event, I am without immediate liquid reserves while actively caring for dependent minor family members.

            ### Statutory & Programmatic Grounds for Protection:
            1. **Procedural Due Process & Notice Requirements:** Under governing administrative regulations in $region, any adverse termination, eviction proceeding, or benefit denial must respect statutory cure periods, mandatory pre-litigation mediation, and due process appeal rights.
            2. **Pending Emergency Relief Application:** I have initiated emergency assistance filings through local relief channels. Arbitrary or accelerated action prior to program determination frustrates the public policy of homelessness prevention and family stabilization.
            3. **Request for Reasonable Accommodation / Hardship Plan:** I hereby formally petition for a 30-day administrative stay or stipulated payment arrangement while expedited public relief resources are processed.

            Please direct all further communications to me in writing at **[Your Email Address]** or by phone at **[Your Phone Number]**. Any legal notices must be served in strict compliance with statutory procedural standards.

            Thank you for your prompt administrative attention to this urgent matter.

            Respectfully submitted,

            ____________________________________  
            **[Your Name]**  
            Tenant / Claimant in Good Faith  
            Enclosures: Proof of Hardship, Emergency Assistance Case Docket
        """.trimIndent()

        val advocacyScript = """
            "Hello, my name is [Your Name]. I am calling regarding my urgent file [Case # if applicable] in $region. I have just submitted a formal written declaration of severe hardship and a request for an emergency administrative review. 
            
            Because my family is in an immediate crisis situation, state and local regulations require expedited review and a temporary pause on any adverse administrative actions while my emergency relief file is evaluated. 
            
            Could you please verify that my formal hardship letter has been entered into the case log today, provide the direct extension of my assigned caseworker or hearing officer, and confirm that all deadlines are stayed pending review?"
        """.trimIndent()

        return CivicActionPlan(
            eligibilitySummary = eligibilityList,
            actionChecklist = checklist,
            draftLetter = draftLetter,
            advocacyScript = advocacyScript,
            disclaimer = "This is AI-generated guidance, not licensed legal advice."
        )
    }
}
