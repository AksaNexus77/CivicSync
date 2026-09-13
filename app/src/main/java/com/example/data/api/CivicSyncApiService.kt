package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.EligibilityItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service interface for communicating with the AI Caseworker engine (Gemini API)
 * to generate localized, empathetic legal aid and social welfare action plans.
 */
interface CivicSyncApiService {

    /**
     * Synthesizes an emergency legal aid and social welfare action plan tailored
     * to the citizen's specific jurisdiction, urgency level, and reported grievance.
     *
     * @param situation Description of the citizen's hardship or denial of benefits.
     * @param urgency Urgency level (e.g., Immediate Crisis, This Week, Planning Ahead).
     * @param country Sovereign nation jurisdiction (e.g., Pakistan, United States, United Kingdom).
     * @param region State, province, or territory within the country.
     * @param language Target language preference for guidance and advocacy scripts.
     * @param uploadedDocuments Verified document labels available in the local vault.
     * @return [Result] containing the parsed [CivicActionPlan] on success, or an exception on failure.
     */
    suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        country: String,
        region: String,
        language: String,
        uploadedDocuments: List<String> = emptyList()
    ): Result<CivicActionPlan>

    /**
     * Generates a deterministic, offline-ready contingency action plan when no
     * network connectivity or Gemini API availability is present.
     */
    fun getOfflineContingencyPlan(
        situation: String,
        country: String,
        region: String,
        uploadedDocuments: List<String> = emptyList()
    ): CivicActionPlan
}

/**
 * Enterprise-grade implementation of [CivicSyncApiService] featuring dynamic prompt
 * injection, token budgeting (<100 word advocacy scripts), strict kotlinx.serialization,
 * regex markdown sanitization, and robust multi-tier fallback mechanisms.
 */
@Singleton
class CivicSyncApiServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : CivicSyncApiService {

    companion object {
        private const val TAG = "CivicSyncApiService"
        private const val GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

        private val JSON_PARSER = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Builds a tailored system instruction dynamically parameterized with the target country,
     * province/region, and language, enforcing strict JSON output and token constraints.
     */
    private fun buildSystemInstruction(country: String, region: String, language: String): String {
        return """
            You are an empathetic, highly knowledgeable legal aid and social welfare caseworker AI.
            Target Citizen Jurisdiction: Country: $country, Province/State/Region: $region, Output Language: $language.
            
            JURISDICTIONAL RULES:
            1. If the country is Pakistan, cite real statutory programs and grievance channels: BISP Kafalat / Benazir Nashonuma, NADRA NRC boards (1777), Sehat Sahulat Program / Sehat Card Plus, EOBI Pensions, Pakistan Bait-ul-Mal, and the Wafaqi Mohtasib (Federal Ombudsman 1055).
            2. If the country is United States (USA), reference SNAP (Food Stamps), Medicaid/Medicare, Section 8 HUD housing vouchers, SSI/SSDI, and local Legal Aid Societies.
            3. If the country is United Kingdom (UK), reference Universal Credit, Personal Independence Payment (PIP), NHS continuing care, Citizen Advice Bureau, and Housing Ombudsman.
            4. If the country is unrecognized or generic, formulate guidance strictly grounded in the Universal Declaration of Human Rights, UN OHCHR guidelines, and national public ombudsman principles.
            
            TOKEN & OUTPUT CONSTRAINTS:
            - Return ONLY a valid, raw JSON object. Do not wrap in markdown quotes or preface with conversational text.
            - IMPORTANT: Limit the 'advocacyScript' field to a MAXIMUM of 100 words to ensure rapid helpline delivery, high verbal clarity, and reduced API latency.
            - Schema requirements:
              {
                "eligibilitySummary": [
                  { "benefit": "String", "reason": "String", "urgency": "String" }
                ],
                "actionChecklist": [
                  { "id": "String (UUID)", "task": "String", "timeline": "String", "category": "String", "isCompleted": false }
                ],
                "draftLetter": "String (Formal administrative representation in clean markdown)",
                "advocacyScript": "String (Maximum 100 words conversational phone/counter script)",
                "localHelplines": [
                  { "title": "String", "contact": "String", "category": "String", "description": "String" }
                ],
                "disclaimer": "String (Statutory non-legal advice notice)"
              }
        """.trimIndent()
    }

    override suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        country: String,
        region: String,
        language: String,
        uploadedDocuments: List<String>
    ): Result<CivicActionPlan> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Check if valid API key is present
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key configured. Generating offline contingency plan.")
            return@withContext Result.success(
                getOfflineContingencyPlan(situation, country, region, uploadedDocuments)
            )
        }

        try {
            val docContext = if (uploadedDocuments.isNotEmpty()) {
                "\nVerified Documents Available in Citizen Vault: ${uploadedDocuments.joinToString(", ")}. Reference these in checklist requirements and formal letter enclosures."
            } else ""

            val userPrompt = """
                Urgency: $urgency
                Country: $country
                Region/State: $region
                Preferred Language: $language
                $docContext

                Citizen Grievance & Hardship Statement:
                $situation
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", buildSystemInstruction(country, region, language))
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

            val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$GEMINI_ENDPOINT?key=$apiKey")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e(TAG, "Gemini API HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(
                    Exception("AI Caseworker service returned error code ${response.code}")
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No candidate response received from AI model"))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Strict Regex fallback to strip accidental markdown block markers before parsing
            val cleanedJson = rawText
                .replace(Regex("^```(?:json|JSON)?\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
                .trim()

            // Strict Kotlinx.Serialization decoding
            val parsedPlan = try {
                JSON_PARSER.decodeFromString<CivicActionPlan>(cleanedJson)
            } catch (ex: Exception) {
                Log.w(TAG, "Strict kotlinx.serialization failed: ${ex.message}. Attempting tolerant fallback parsing.")
                parseFallbackJson(cleanedJson, situation, country, region)
            }

            Result.success(parsedPlan)
        } catch (networkEx: Exception) {
            Log.e(TAG, "Network or processing exception during plan generation: ${networkEx.message}", networkEx)
            Result.failure(networkEx)
        }
    }

    /**
     * Fallback parser using org.json if model produced minor formatting deviations.
     */
    private fun parseFallbackJson(
        jsonString: String,
        situation: String,
        country: String,
        region: String
    ): CivicActionPlan {
        return try {
            val obj = JSONObject(jsonString)
            val eligList = mutableListOf<EligibilityItem>()
            val eligArr = obj.optJSONArray("eligibilitySummary")
            if (eligArr != null) {
                for (i in 0 until eligArr.length()) {
                    val item = eligArr.optJSONObject(i) ?: continue
                    eligList.add(
                        EligibilityItem(
                            benefit = item.optString("benefit", "Social Protection Program"),
                            reason = item.optString("reason", "Meets hardship criteria"),
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
                            task = item.optString("task", "File administrative representation"),
                            timeline = item.optString("timeline", "Immediate"),
                            category = item.optString("category", "General"),
                            isCompleted = item.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            val helplineList = mutableListOf<com.example.data.model.HelplineItem>()
            val helplineArr = obj.optJSONArray("localHelplines")
            if (helplineArr != null) {
                for (i in 0 until helplineArr.length()) {
                    val item = helplineArr.optJSONObject(i) ?: continue
                    helplineList.add(
                        com.example.data.model.HelplineItem(
                            title = item.optString("title", "Emergency Helpline"),
                            contact = item.optString("contact", "1055"),
                            category = item.optString("category", "Legal Aid & Welfare"),
                            description = item.optString("description", "")
                        )
                    )
                }
            }

            CivicActionPlan(
                eligibilitySummary = eligList.ifEmpty {
                    listOf(EligibilityItem("Emergency Public Relief", "Based on submitted grievance statement", "Immediate"))
                },
                actionChecklist = checkList.ifEmpty {
                    listOf(ChecklistItem(task = "Visit designated municipal or social welfare desk in $region", timeline = "Within 48h", category = "Administrative"))
                },
                draftLetter = obj.optString("draftLetter", "Formal representation regarding citizen hardship in $region, $country."),
                advocacyScript = obj.optString("advocacyScript", "Hello, I am calling regarding my urgent grievance in $region. Please provide my tracking number."),
                localHelplines = helplineList.ifEmpty { getDefaultHelplines(country, region) },
                disclaimer = obj.optString("disclaimer", "AI-generated guidance, not licensed legal advice.")
            )
        } catch (e: Exception) {
            getOfflineContingencyPlan(situation, country, region)
        }
    }

    private fun getDefaultHelplines(country: String, region: String): List<com.example.data.model.HelplineItem> {
        val isPakistan = country.contains("Pakistan", ignoreCase = true) || country.isBlank()
        return if (isPakistan) {
            listOf(
                com.example.data.model.HelplineItem("Wafaqi Mohtasib (Federal Ombudsman)", "1055", "Tribunal", "Free lodging of maladministration complaints against any federal department"),
                com.example.data.model.HelplineItem("BISP Crisis Cash Helpline", "0800-26477", "Social Protection", "Toll-free verification of Benazir Kafalat enrollment"),
                com.example.data.model.HelplineItem("NADRA Citizen Care", "1777", "Civil Identity", "Direct line for biometric issues, blocked CNICs, and executive center assistance"),
                com.example.data.model.HelplineItem("Legal Aid Society Pakistan", "0800-70806", "Legal Aid", "Free legal advice for vulnerable citizens and women")
            )
        } else {
            listOf(
                com.example.data.model.HelplineItem("Public Grievance Redressal Desk", "211", "Public Aid", "Community support & legal advocacy services in $region")
            )
        }
    }

    /**
     * Generates a comprehensive, offline-first contingency plan when internet is unavailable.
     */
    override fun getOfflineContingencyPlan(
        situation: String,
        country: String,
        region: String,
        uploadedDocuments: List<String>
    ): CivicActionPlan {
        val isPakistan = country.contains("Pakistan", ignoreCase = true) || country.isBlank()
        val isUSA = country.contains("United States", ignoreCase = true) || country.contains("USA", ignoreCase = true)
        val isUK = country.contains("United Kingdom", ignoreCase = true) || country.contains("UK", ignoreCase = true)

        val docNotice = if (uploadedDocuments.isNotEmpty()) {
            "\n(Verified Documents in Vault: ${uploadedDocuments.joinToString(", ")})"
        } else ""

        val eligibility = when {
            isPakistan -> listOf(
                EligibilityItem(
                    benefit = "Benazir Income Support Programme (BISP Kafalat)",
                    reason = "Quarterly poverty alleviation grant under dynamic registry and emergency relief.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "NADRA Priority Biometric Verification & Redressal Board",
                    reason = "Administrative redressal for fingerprint fading, blocked CNICs, and FRC disputes.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Sehat Sahulat Program / Sehat Card Plus ($region)",
                    reason = "100% cashless indoor hospitalization and emergency surgical treatment in empaneled hospitals.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Wafaqi Mohtasib (Federal Ombudsman Secretariat)",
                    reason = "Statutory free tribunal holding jurisdiction over federal agencies for administrative maladministration.",
                    urgency = "Planning Ahead"
                )
            )
            isUSA -> listOf(
                EligibilityItem(
                    benefit = "SNAP (Supplemental Nutrition Assistance Program)",
                    reason = "Monthly food budget assistance based on household income and emergency crisis standards.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Medicaid & Emergency Medical Assistance ($region)",
                    reason = "Low-cost or free health coverage for eligible low-income adults, children, and elderly.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Legal Services Corporation (LSC) Pro Bono Defense",
                    reason = "Federally funded civil legal assistance for housing, eviction, and administrative benefits.",
                    urgency = "This Week"
                )
            )
            isUK -> listOf(
                EligibilityItem(
                    benefit = "Universal Credit (DWP Emergency Hardship Payment)",
                    reason = "Emergency advance and standard allowance for living and housing expenses.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Citizens Advice Bureau Legal Navigator",
                    reason = "Confidential, independent legal and benefits counseling.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Local Council Crisis Support Grant ($region)",
                    reason = "Emergency discretionary housing payments and food support.",
                    urgency = "Immediate Crisis"
                )
            )
            else -> listOf(
                EligibilityItem(
                    benefit = "Universal Declaration of Human Rights (UDHR Art. 25 & 8)",
                    reason = "Right to an adequate standard of living, healthcare, security, and effective remedy before tribunals.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "National Human Rights Commission / Public Ombudsman",
                    reason = "Statutory avenue for administrative grievance redressal against state entities in $country.",
                    urgency = "This Week"
                )
            )
        }

        val checklist = when {
            isPakistan -> listOf(
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Visit nearest NADRA Executive Center in $region with CNIC & Family Registration Certificate$docNotice",
                    timeline = "Day 1 (Immediate)",
                    category = "Civil Identity (NADRA)"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Send 13-digit CNIC to 8171 SMS service and register at BISP Tehsil Desk in $region",
                    timeline = "Day 1-2",
                    category = "Social Protection (BISP)"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Call Wafaqi Mohtasib Helpline 1055 or lodge grievance at www.mohtasib.gov.pk for maladministration",
                    timeline = "Within 48 Hours",
                    category = "Ombudsman Appeal"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Submit formal written hardship representation to the Deputy Commissioner Office / Grievance Cell in $region",
                    timeline = "This Week",
                    category = "District Administration"
                )
            )
            isUSA -> listOf(
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Submit emergency expedited SNAP and Medicaid application through your State Benefits Portal in $region",
                    timeline = "Within 24 Hours",
                    category = "Food & Healthcare"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Contact local Legal Aid Society or call 211 for eviction defense and administrative representation",
                    timeline = "Immediate",
                    category = "Legal Aid"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "File formal Fair Hearing Request regarding wrongful benefit termination or reduction",
                    timeline = "Within 10 Days",
                    category = "Administrative Appeal"
                )
            )
            isUK -> listOf(
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Request an immediate Universal Credit Short-Term Advance through your DWP journal or helpline 0800 328 5644",
                    timeline = "Within 24 Hours",
                    category = "Social Security"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Schedule urgent appointment with Citizens Advice Bureau in $region",
                    timeline = "Day 2",
                    category = "Advisory"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Submit formal Mandatory Reconsideration notice against unfavorable decision",
                    timeline = "Within 30 Days",
                    category = "Tribunal Appeal"
                )
            )
            else -> listOf(
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Assemble national identity cards, residence permits, official denial letters, and utility receipts$docNotice",
                    timeline = "Immediate",
                    category = "Documentation"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "File written representation with the Ministry of Social Affairs or Municipal Grievance Cell in $region, $country",
                    timeline = "Within 48 Hours",
                    category = "Public Administration"
                ),
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    task = "Contact local Bar Association or UN Refugee/Human Rights office for emergency civil legal assistance",
                    timeline = "This Week",
                    category = "Legal Advocacy"
                )
            )
        }

        val draftLetter = """
            # FORMAL ADMINISTRATIVE REPRESENTATION & APPEAL FOR EXPEDITED RELIEF
            
            **Date:** [Current Date]  
            **To:**  
            The Director General / Public Grievance Officer  
            Administrative Grievance Redressal Secretariat, $region, $country  
            
            **Subject: URGENT CITIZEN GRIEVANCE PETITION REGARDING SOCIO-ECONOMIC DISTRESS AND BENEFIT ACCESS**
            
            Respected Authority,
            
            I, **[Citizen Full Name]**, holding National Identity Number: **[ID Number]**, residing in **[Residential Address]**, $region, respectfully submit:
            
            1. That the applicant is facing extreme hardship and administrative vulnerability: *"$situation"*.
            2. That despite lawful eligibility under prevailing statutory welfare standards, procedural delays and verification bottlenecks have deprived the applicant of fundamental rights.
            3. That the applicant has enclosed bona fide verification records seeking immediate administrative rectification.
            
            ### Specific Relief Requested:
            - Immediate biometric or administrative review of applicant's file.
            - Disbursement of emergency interim relief or medical subsidy.
            - Written acknowledgment and official grievance diary number.
            
            Respectfully submitted,  
            ___________________________________  
            **[Citizen Full Name]**  
            ID: **[ID Number]** | Contact: **[Phone Number]**  
            Address: **[Address, $region, $country]**
        """.trimIndent()

        val advocacyScript = when {
            isPakistan -> "Assalam-o-Alaikum, mera naam [Name] hai, CNIC: [CNIC]. Main $region se call kar raha/rahi hoon regarding my emergency appeal. Baraye meharbani meri complaint ka tracking number register karein aur batayein k Tehsil officer se kab rabta hoga. Bohat shukriya."
            isUSA -> "Hello, my name is [Name], case ID [ID]. I am calling from $region regarding my expedited assistance application. I am experiencing an urgent crisis and need to confirm my case status and request an immediate supervisor review. Thank you."
            isUK -> "Good day, my name is [Name], National Insurance number [NI]. I am calling regarding my urgent benefit application in $region. I require an emergency hardship decision today and need my incident reference number recorded."
            else -> "Hello, my name is [Name], ID number [ID]. I am contacting your office from $region regarding an urgent administrative grievance. Please confirm the receipt of my petition and provide my official inquiry reference number."
        }

        return CivicActionPlan(
            eligibilitySummary = eligibility,
            actionChecklist = checklist,
            draftLetter = draftLetter,
            advocacyScript = advocacyScript,
            localHelplines = getDefaultHelplines(country, region),
            disclaimer = "Statutory Notice: This is AI-synthesized guidance, not licensed legal advice. Please consult your local legal aid society or competent public authority."
        )
    }
}
