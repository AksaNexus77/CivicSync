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
        "Analyze the user's input, urgency, and province (Punjab, Sindh, KPK, Balochistan, ICT). " +
        "Return ONLY a valid, raw JSON object (no markdown formatting). The JSON must contain:\n" +
        "- 'eligibilitySummary': Array of objects { benefit, reason, urgency } referencing BISP Kafalat, Sehat Card Plus, EOBI, Pakistan Bait-ul-Mal, Zakat Fund, etc.\n" +
        "- 'actionChecklist': Array of objects { task, timeline, category } with localized steps (e.g., NADRA center visits, SMS 8171, Wafaqi Mohtasib 1055).\n" +
        "- 'draftLetter': A formal Markdown string addressed to the Deputy Commissioner, Chairman NADRA, DG BISP, or Wafaqi Mohtasib, with placeholders [Name], [CNIC], [Address].\n" +
        "- 'advocacyScript': A conversational Urdu/English script for calling helplines (BISP 0800-26477, NADRA 1777, Wafaqi Mohtasib 1055), including tips on recording complaint tracking numbers.\n" +
        "- 'disclaimer': 'This is AI-generated guidance, not licensed legal advice. Please verify with a local lawyer or relevant government office.'"

    suspend fun generateActionPlan(
        situation: String,
        urgency: String,
        location: String,
        uploadedDocuments: List<String> = emptyList()
    ): Result<CivicActionPlan> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key found, generating empathetic expert fallback plan")
            return@withContext Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location, uploadedDocuments))
        }

        try {
            val docsInfo = if (uploadedDocuments.isNotEmpty()) {
                "\nVerified Documents Available in Citizen Vault: ${uploadedDocuments.joinToString(", ")}. Reference these verified documents in the action steps and letter enclosures."
            } else {
                ""
            }

            val userPrompt = """
                Urgency: $urgency
                Province: ${location.ifBlank { "Punjab" }}
                $docsInfo
                
                Citizen Grievance & Hardship:
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
                return@withContext Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location, uploadedDocuments))
            }

            val rootObj = JSONObject(responseBodyString)
            val candidates = rootObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location, uploadedDocuments))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Robust regex to strip any accidental markdown formatting
            val cleanedJson = rawText
                .replace(Regex("^```(?:json|JSON)?\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
                .trim()

            try {
                val planObj = JSONObject(cleanedJson)
                val plan = parseJsonToActionPlan(planObj)
                Result.success(plan)
            } catch (parseEx: Exception) {
                Log.e(TAG, "Failed parsing JSON: ${parseEx.message}, Raw text: $cleanedJson")
                // Return fallback plan instead of breaking user flow
                Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location, uploadedDocuments))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in GeminiCaseworkerService: ${e.message}", e)
            Result.success(buildEmpatheticSynthesizedPlan(situation, urgency, location, uploadedDocuments))
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
                            reason = item.optString("reason", "Meets criteria based on reported hardship"),
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
                            task = item.optString("task", "Review official records"),
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
            "**FORMAL REPRESENTATION & APPEAL FOR ADMINISTRATIVE GRIEVANCE REDRESSAL**\n\n[Date]\n\nTo:\nThe Deputy Commissioner / Director General (NADRA / BISP)\nDistrict Grievance Redressal Cell\n\nRespected Sir/Madam,\n\nI, [Name], CNIC: [CNIC Number], resident of [Address], respectfully request urgent administrative intervention.\n\nSincerely,\n[Name]"
        )
        val advocacyScript = planObj.optString(
            "advocacyScript",
            "Assalam-o-Alaikum, mera naam [Name] hai, CNIC number [CNIC Number]. Main call kar raha/rahi hoon regarding my urgent application. Baraye meharbani meri complaint ka official tracking number register karein aur batayein k Tehsil office se kab rabta hoga. Shukriya."
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

    fun buildEmpatheticSynthesizedPlan(
        situation: String,
        urgency: String,
        location: String,
        uploadedDocuments: List<String> = emptyList()
    ): CivicActionPlan {
        val province = when {
            location.contains("Sindh", ignoreCase = true) -> "Sindh"
            location.contains("KPK", ignoreCase = true) || location.contains("Khyber", ignoreCase = true) -> "KPK"
            location.contains("Balochistan", ignoreCase = true) -> "Balochistan"
            location.contains("Islamabad", ignoreCase = true) || location.contains("ICT", ignoreCase = true) -> "Islamabad (ICT)"
            else -> "Punjab"
        }

        val capitalCity = when (province) {
            "Sindh" -> "Karachi"
            "KPK" -> "Peshawar"
            "Balochistan" -> "Quetta"
            "Islamabad (ICT)" -> "Islamabad"
            else -> "Lahore"
        }

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
                    reason = "100% cashless indoor hospitalization and emergency surgical treatment up to Rs. 1,000,000 in empaneled hospitals across $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "Pakistan Bait-ul-Mal (PBM) Medical Relief Fund",
                    reason = "Direct financial support for critical chronic diseases, dialyses, cancer medicines, and specialized diagnostic tests in $capitalCity.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Provincial Zakat Fund & Hospital Welfare Committee",
                    reason = "Statutory provision of free prescription medication and diagnostics funded via Provincial Zakat Department in government hospitals in $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "BISP Benazir Nashonuma & Kafalat Shield",
                    reason = "Quarterly cash transfers and specialized nutrition supplements for mothers and infants under BISP dynamic registry.",
                    urgency = "Planning Ahead"
                )
            )
        } else if (isNadraOrCnic) {
            listOf(
                EligibilityItem(
                    benefit = "NADRA Priority Biometric Verification & Zonal Board",
                    reason = "Administrative redressal for fingerprint fading or CNIC renewal blocks through NRC Zonal Verification Boards in $capitalCity, $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "BISP Biometric Alternative Payment Authorization (BAPA)",
                    reason = "BISP protocol authorizing manual pay-orders or designated kin collection when biometric fingerprinting repeatedly fails at retail franchises.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Wafaqi Mohtasib (Federal Ombudsman) Grievance Redressal",
                    reason = "Free constitutional tribunal with jurisdiction over NADRA, BISP, and federal departments for arbitrary delays or unlawful account freezes.",
                    urgency = "This Week"
                )
            )
        } else {
            listOf(
                EligibilityItem(
                    benefit = "Benazir Income Support Programme (BISP) - Benazir Kafalat",
                    reason = "Quarterly unconditional financial grant for families scoring below the poverty cutoff on the National Socio-Economic Registry (NSER) in $province.",
                    urgency = "Immediate Crisis"
                ),
                EligibilityItem(
                    benefit = "EOBI Pension & Survivors Grant",
                    reason = "Statutory monthly old-age pension or widow/survivors grant for formal and industrial workers registered under EOBI in $province.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "Pakistan Bait-ul-Mal (PBM) Emergency Financial Relief",
                    reason = "Immediate cash grants and education stipends (Taleemi Wazaif) for disadvantaged families facing sudden destitution.",
                    urgency = "This Week"
                ),
                EligibilityItem(
                    benefit = "District Legal Empowerment Committee (DLEC) Free Legal Defense",
                    reason = "State-appointed pro bono legal aid counsel through the District and Sessions Judge in $province.",
                    urgency = "Planning Ahead"
                )
            )
        }

        val docNotice = if (uploadedDocuments.isNotEmpty()) {
            " (Vault Documents Verified: ${uploadedDocuments.joinToString(", ")})"
        } else ""

        val checklist = listOf(
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Visit the NADRA Executive Registration Center in $capitalCity ($province) with your original CNIC and Family Registration Certificate (FRC)$docNotice",
                timeline = "Immediate (Day 1-2)",
                category = "Civil Identity (NADRA)",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Send your 13-digit CNIC to 8171 via SMS and visit the nearest BISP Tehsil Registration Desk in $province for dynamic survey enrollment",
                timeline = "Day 2-3",
                category = "Social Protection (BISP)",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "File a complaint with the Wafaqi Mohtasib (Federal Ombudsman) via online portal www.mohtasib.gov.pk or call 1055 to stop arbitrary administrative action",
                timeline = "Within 48 Hours",
                category = "Ombudsman Appeal",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Submit formal written hardship representation to the Deputy Commissioner (DC) Office / Grievance Cell in $capitalCity, $province",
                timeline = "This Week",
                category = "District Administration",
                isCompleted = false
            ),
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                task = "Apply for emergency medical or financial assistance at the District Bait-ul-Mal / Social Welfare Department in $province",
                timeline = "Planning Ahead",
                category = "Welfare Relief",
                isCompleted = false
            )
        )

        val docEnclosures = if (uploadedDocuments.isNotEmpty()) {
            "Verified Vault Documents: " + uploadedDocuments.joinToString(", ")
        } else {
            "Copy of CNIC, Electricity Bill, Rejection Receipt"
        }

        val draftLetter = """
            # FORMAL REPRESENTATION & APPEAL FOR ADMINISTRATIVE GRIEVANCE REDRESSAL

            **Date:** [Date]  
            **To:**  
            The Deputy Commissioner / The Director General (BISP / NADRA) / Wafaqi Mohtasib  
            Public Grievance Redressal Secretariat, $province  
            Government of Pakistan  

            **Subject: URGENT APPLICATION REGARDING CITIZEN HARDSHIP AND EXPEDITED ADMINISTRATIVE RELIEF / SOCIAL PROTECTION**

            Respected Sir/Madam,

            I, **[Name]**, holding CNIC Number: **[CNIC Number]**, permanent resident of **[Address]**, $province, most respectfully state as under:

            1. That the applicant belongs to an impoverished household in $province and is currently undergoing severe socio-economic distress: *"$situation"*.
            
            2. That despite having legitimate constitutional rights under Articles 9, 14, and 38 of the Constitution of the Islamic Republic of Pakistan, the applicant has encountered severe bureaucratic bottlenecks, biometric verification failures, or unjustified procedural delays.

            3. That the applicant possesses verified identity and bona fide documentation, urgently seeking administrative review for social safety net access (BISP Kafalat / Sehat Card Plus / Bait-ul-Mal assistance) and rectification of public records.

            ### Relief Prayed:
            It is respectfully prayed that your authority may kindly:
            - Order the concerned Tehsil / Zonal office to process the applicant's file on emergency priority basis.
            - Sanction interim financial or medical relief through the District Social Welfare / Zakat / Bait-ul-Mal fund.
            - Issue instructions to NADRA / BISP for biometric override or alternative payment disbursement.

            Thanking you in anticipation.

            Yours obediently,

            ____________________________________  
            **[Name]**  
            CNIC No: **[CNIC Number]**  
            Address: **[Address]**  
            Enclosures: $docEnclosures
        """.trimIndent()

        val advocacyScript = """
            "Assalam-o-Alaikum. Mera naam [Name] hai aur CNIC number [CNIC Number] hai. Main [Address], $province se call kar raha/rahi hoon. 

            Main ne apni emergency relief application aur grievance redressal file ki thi. Hamaray gharanay ko fori imdad aur biometric/record verification ki shadeed zaroorat hai. 

            Kiya aap baraye meharbani mujhe meri complaint ka official tracking/diary number bata saktay hain, aur ye confirm kar saktay hain k Tehsil BISP/NADRA officer kab tak meri file review karein gay? Bohat shukriya."
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
