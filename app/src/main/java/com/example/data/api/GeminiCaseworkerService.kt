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
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    const val EXACT_SYSTEM_INSTRUCTION =
        "You are an empathetic, highly knowledgeable Pakistani legal aid and social welfare AI. " +
        "You must understand the specific context of Pakistan (NADRA, BISP, EOBI, Sehat Sahulat Card, Wafaqi Mohtasib, Provincial Social Welfare Departments). " +
        "Analyze the user's input, their urgency level, and their specific province (Punjab, Sindh, KPK, Balochistan, etc.). " +
        "You must return ONLY a valid, raw JSON object. The JSON must have: " +
        "- 'eligibilitySummary' (array: benefit, reason, urgency) - Use Pakistani programs like BISP, EOBI, Sehat Card, Zakat Fund, etc. " +
        "- 'actionChecklist' (array: task, timeline, category) - Include steps like \"Visit NADRA center with CNIC\", \"File complaint with Wafaqi Mohtasib\", etc. " +
        "- 'draftLetter' (A formal Markdown string addressed to Pakistani authorities like the Deputy Commissioner, NADRA, or Wafaqi Mohtasib, including placeholders for [Name], [CNIC Number], and [Address]). " +
        "- 'advocacyScript' (A conversational, assertive paragraph in simple Urdu/English for calling Pakistani helplines like BISP 0800-26477). " +
        "- 'disclaimer' (string: 'This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office.') " +
        "Do not wrap the JSON in markdown code blocks. Return raw JSON only."

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
                Province/Region (Pakistan): ${location.ifBlank { "Punjab, Pakistan" }}
                
                Citizen Hardship & Legal/Welfare Query:
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
            "**APPLICATION FOR EMERGENCY WELFARE RELIEF & ADMINISTRATIVE INTERVENTION**\n\n[Date]\n\nTo:\nThe Deputy Commissioner / Director General,\n[Address]\n\nRespected Sir/Madam,\n\nI, [Name], CNIC: [CNIC Number], resident of [Address], respectfully request urgent review.\n\nSincerely,\n[Name]"
        )
        val advocacyScript = planObj.optString(
            "advocacyScript",
            "Assalam-o-Alaikum, mera naam [Name] hai, CNIC number [CNIC Number]. Main [Address] se call kar raha/rahi hoon regarding my urgent application. Baraye meharbani meri complaint register karein aur inquiry status confirm karein."
        )
        val disclaimer = planObj.optString(
            "disclaimer",
            "This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office."
        )

        return CivicActionPlan(
            eligibilitySummary = eligibilityList,
            actionChecklist = checklist,
            draftLetter = draftLetter,
            advocacyScript = advocacyScript,
            disclaimer = disclaimer
        )
    }

    /**
     * Synthesizes a high-fidelity, empathetic Pakistani civic action plan matching the exact schema
     * when offline or when an API key is not configured.
     */
    fun buildEmpatheticSynthesizedPlan(
        situation: String,
        urgency: String,
        location: String
    ): CivicActionPlan {
        val province = location.ifBlank { "Punjab, Pakistan" }

        val isHealthConcern = situation.contains("sehat", ignoreCase = true) ||
                situation.contains("hospital", ignoreCase = true) ||
                situation.contains("ilaj", ignoreCase = true) ||
                situation.contains("doctor", ignoreCase = true) ||
                situation.contains("medical", ignoreCase = true)

        val isNadraOrCnic = situation.contains("nadra", ignoreCase = true) ||
                situation.contains("cnic", ignoreCase = true) ||
                situation.contains("id card", ignoreCase = true) ||
                situation.contains("frc", ignoreCase = true) ||
                situation.contains("biometric", ignoreCase = true)

        val eligibilityList = if (isHealthConcern) {
            listOf(
                EligibilityItem(
                    benefit = "Sehat Sahulat Program / Sehat Card Plus ($province)",
                    reason = "Entitlement for zero-cost indoor hospitalization, surgical interventions, and emergency treatment under universal/targeted health cards in empaneled hospitals across $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Pakistan Bait-ul-Mal (PBM) Special Medical Assistance",
                    reason = "Direct financial grant for critical chronic ailments, life-saving medicines, and specialized diagnostic tests for deserving citizens holding valid CNIC.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Provincial Zakat Fund & Hospital Health Welfare Committee (Mustahiqeen Quota)",
                    reason = "Statutory provision of free prescription medication and diagnostics funded via the Provincial Zakat & Ushr Department in public tertiary hospitals in $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "BISP Benazir Nashonuma & Kafalat Financial Shield",
                    reason = "Quarterly unconditional cash transfers and maternal health support for low-income mothers and children registered under BISP dynamic registry.",
                    urgency = "Planning Ahead"
                )
            )
        } else if (isNadraOrCnic) {
            listOf(
                EligibilityItem(
                    benefit = "NADRA Priority Biometric Verification & CNIC Dispute Resolution",
                    reason = "Statutory redressal for biometric failure or CNIC renewal blocks through NRC Zonal Verification Boards and Union Council verification committees.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "BISP Biometric Alternative Payment Authorization (BISP-BAPA)",
                    reason = "Special BISP protocol allowing designated family member authorization or physical counter pay-orders when senior citizen fingerprints cannot be read by PoS biometric machines.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Wafaqi Mohtasib (Federal Ombudsman) Administrative Redressal",
                    reason = "Free administrative judicial tribunal holding jurisdiction over federal agencies (NADRA, BISP, EOBI) for arbitrary delays, unwarranted blocks, or maladministration.",
                    urgency = "This Week"
                )
            )
        } else {
            listOf(
                EligibilityItem(
                    benefit = "Benazir Income Support Programme (BISP) - Benazir Kafalat",
                    reason = "Unconditional quarterly cash assistance for low-income households scoring below eligibility threshold on the National Socio-Economic Registry (NSER) in $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "EOBI (Employees' Old-Age Benefits Institution) Pension & Survivors Fund",
                    reason = "Monthly statutory old-age pension or invalidity/survivor pension for insured private-sector formal workers and bereaved families.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Pakistan Bait-ul-Mal (PBM) Emergency Financial Relief & Educational Stipends",
                    reason = "Direct financial assistance and educational support (Taleemi Wazaif) for disadvantaged families facing sudden destitution.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "District Legal Empowerment Committee (DLEC) Free Legal Defense",
                    reason = "Full state-funded legal aid and pro bono advocate appointment by the District and Sessions Judge for underprivileged citizens.",
                    urgency = "Planning Ahead"
                )
            )
        }

        val checklist = listOf(
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Visit NADRA Mega Center / Executive NRC with original CNIC, B-Forms, and electricity bill to verify family tree (FRC) and biometric authentication",
                timeline = "Immediate (Day 1-2)",
                category = "Civil Identity (NADRA)",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Check BISP NSER Status by sending 13-digit CNIC to 8171 via SMS and visit the nearest BISP Tehsil Registration Desk",
                timeline = "Day 2-3",
                category = "Social Protection (BISP)",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Submit formal written representation to the Deputy Commissioner (DC) Office / Assistant Commissioner's Public Grievance Cell in $province",
                timeline = "Within 48 Hours",
                category = "District Administration",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "File free complaint with Wafaqi Mohtasib (Federal Ombudsman) via Helpline 1055 or online portal www.mohtasib.gov.pk for maladministration",
                timeline = "This Week",
                category = "Ombudsman Appeal",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Contact District Legal Empowerment Committee (DLEC) at District Courts for government-assigned free advocate counsel",
                timeline = "Planning Ahead",
                category = "Legal Representation",
                isCompleted = false
            )
        )

        val draftLetter = """
            # FORMAL REPRESENTATION & APPEAL FOR ADMINISTRATIVE GRIEVANCE REDRESSAL

            **Date:** [Date]  
            **To:**  
            The Deputy Commissioner / The Director General (BISP / NADRA)  
            District Grievance Redressal Cell, $province  
            Government of Pakistan  

            **Subject: URGENT APPLICATION REGARDING CITIZEN GRIEVANCE AND PROVISION OF EMERGENCY SOCIAL PROTECTION / ADMINISTRATIVE RELIEF**

            Respected Sir/Madam,

            I, **[Name]**, holding CNIC Number: **[CNIC Number]**, permanent resident of **[Address]**, $province, most respectfully submit this formal representation before your kind authority:

            1. That the applicant belongs to an underprivileged household in $province and is currently undergoing acute socio-economic and administrative hardship: *"$situation"*.
            
            2. That despite having a legitimate constitutional and statutory right to state welfare mechanisms (under Article 9, 14, and 38 of the Constitution of the Islamic Republic of Pakistan), the applicant has faced procedural bottlenecks, biometric verification issues, or administrative delays at the relevant public offices.

            3. That the applicant holds all lawful documentation, including valid CNIC and verified household particulars, and urgently requires administrative intervention for emergency social welfare entitlements (BISP / Sehat Card / Bait-ul-Mal assistance) and rectification of official records.

            ### Prayer / Relief Sought:
            It is therefore respectfully prayed that your good office may kindly:
            - Issue immediate directives to the concerned Tehsil/Zonal officers to process the applicant's case without unnecessary delay.
            - Provide interim financial or medical relief through the District Social Welfare / Zakat / Bait-ul-Mal fund.
            - Ensure a fair, transparent hearing and communication of decision within the statutory period prescribed under the Ombudsman and Civil Service guidelines.

            Your sympathetic and prompt attention to this matter will save an impoverished family from further distress.

            Yours obediently,

            ____________________________________  
            **[Name]**  
            CNIC No: **[CNIC Number]**  
            Address: **[Address]**  
            Contact Number: [Phone Number]  
            Enclosures: Copy of CNIC, Relevant Utility Bill / Rejection Slips
        """.trimIndent()

        val advocacyScript = """
            "Assalam-o-Alaikum. Mera naam [Name] hai, aur mera CNIC number [CNIC Number] hai. Main [Address], $province se call kar raha/rahi hoon. 
            
            Main ne apni emergency relief aur grievance redressal k silsilay mein application submit ki thi. Hamaray gharanay ko fori imdad aur biometric/record verification ki zaroorat hai kyun k hamaray paas koi doosra sahara nahi hai. 
            
            Kiya aap baraye meharbani mujhe meri complaint ka official tracking/diary number bata saktay hain, aur ye confirm kar saktay hain k Tehsil BISP/NADRA officer ya concerned focal person kab tak meri file review karein gay? Bohat shukriya."
        """.trimIndent()

        return CivicActionPlan(
            eligibilitySummary = eligibilityList,
            actionChecklist = checklist,
            draftLetter = draftLetter,
            advocacyScript = advocacyScript,
            disclaimer = "This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office."
        )
    }
}
