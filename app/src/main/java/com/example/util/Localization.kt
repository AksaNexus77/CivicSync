package com.example.util

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val labelEn: String, val labelNative: String) {
    ENGLISH("en", "English", "English"),
    URDU("ur", "Urdu", "اردو")
}

object UrduTypography {
    val NotoNastaliqUrduFamily: FontFamily by lazy {
        try {
            // Attempt to load system Noto Nastaliq Urdu typeface
            val tf = Typeface.create("Noto Nastaliq Urdu", Typeface.NORMAL)
            if (tf != Typeface.DEFAULT) {
                FontFamily(tf)
            } else {
                val arabicTf = Typeface.create("serif", Typeface.NORMAL)
                FontFamily(arabicTf)
            }
        } catch (e: Throwable) {
            FontFamily.Serif
        }
    }
}

fun AppLanguage.layoutDirection(): LayoutDirection = when (this) {
    AppLanguage.ENGLISH -> LayoutDirection.Ltr
    AppLanguage.URDU -> LayoutDirection.Rtl
}

object Strings {
    fun get(key: String, lang: AppLanguage): String {
        val entry = dictionary[key] ?: return key
        return when (lang) {
            AppLanguage.ENGLISH -> entry.first
            AppLanguage.URDU -> entry.second
        }
    }

    private val dictionary = mapOf(
        // App Header & Nav
        "app_title" to Pair("CivicSync Pakistan", "سوِک سِنک پاکستان"),
        "app_subtitle" to Pair("Empathetic Legal Aid & Welfare Navigator", "قانونی و فلاحی رہنماء برائے پاکستانی شہری"),
        "nav_home" to Pair("Home", "ہوم"),
        "nav_active_cases" to Pair("Active Cases", "محفوظ شدہ کیسز"),
        "nav_vault" to Pair("Document Vault", "دستاویزات والٹ"),
        "nav_offline" to Pair("Offline Guides", "آف لائن رہنمائی"),
        "nav_resources" to Pair("Helplines", "ہیلپ لائنز"),
        "nav_settings" to Pair("Settings", "ترتیبات"),

        // Intake Screen
        "hero_badge" to Pair("PAKISTAN LEGAL AID & SOCIAL WELFARE AI", "پاکستانی قانونی امداد و سماجی تحفظ اے آئی"),
        "hero_title" to Pair("You don't have to face the system alone.", "آپ کو اکیلے نظام کا سامنا کرنے کی ضرورت نہیں۔"),
        "hero_desc" to Pair(
            "Empathetic, localized guidance for BISP Kafalat, NADRA CNIC, Sehat Sahulat Card, EOBI Pension, and Wafaqi Mohtasib administrative appeals.",
            "نادرا، بے نظیر انکم سپورٹ، صحت کارڈ، ای او بی آئی اور وفاقی محتسب کے معاملات میں قانونی و انتظامی مدد حاصل کریں۔"
        ),
        "preset_title" to Pair("Select a sample citizen crisis:", "کسی عام شہری مسئلے کا نمونہ منتخب کریں:"),
        "step1_title" to Pair("1. Describe Your Hardship", "1. اپنا مسئلہ یا صورتحال بیان کریں"),
        "step1_subtitle" to Pair("Include CNIC issues, hospital denial, BISP stops, or landlord notices.", "شناختی کارڈ کے مسائل، ہسپتال میں انکار، یا وظیفہ بند ہونے کی تفصیل لکھیں۔"),
        "textarea_placeholder" to Pair(
            "e.g., Meri walida ka BISP biometric verify nahi ho raha aur hospital mein Sehat Card reject kar diya hai. Hamaray paas ration k paise nahi hain. Ya NADRA CNIC block hai.",
            "مثال: میری والدہ کی بی آئی ایس پی رقم بائیو میٹرک فنگر پرنٹ نہ لگنے کی وجہ سے رکی ہوئی ہے، یا ہسپتال نے صحت کارڈ پر داخلہ دینے سے انکار کر دیا ہے..."
        ),
        "speak_to_type" to Pair("Speak in Urdu/English", "بول کر لکھوائیں"),
        "speaking_indicator" to Pair("Listening... Speak now", "سماعت جاری ہے... اب بولیے"),
        "step2_title" to Pair("2. Urgency & Province", "2. فوری ضرورت اور صوبہ"),
        "step2_subtitle" to Pair("Provincial welfare rules and legal forums differ across Pakistan.", "صوبائی فلاحی اسکیمیں اور محتسب دفاتر صوبے کے لحاظ سے مختلف ہیں۔"),
        "urgency_label" to Pair("Urgency Level", "فوری ضرورت کا درجہ"),
        "province_label" to Pair("Province / Region (Pakistan)", "صوبہ / علاقہ (پاکستان)"),
        "generate_btn" to Pair("Generate Action Plan ✨", "قانونی لائحۂ عمل تیار کریں ✨"),

        // Presets
        "preset_bisp" to Pair("BISP Biometric Error", "بے نظیر بائیو میٹرک خرابی"),
        "preset_sehat" to Pair("Sehat Card Hospital Denial", "صحت کارڈ ہسپتال انکار"),
        "preset_nadra" to Pair("NADRA Blocked CNIC", "نادرا بلاک شناختی کارڈ"),
        "preset_eobi" to Pair("EOBI Pension Delay", "ای او بی آئی پنشن تاخیر"),

        // Urgency options
        "urgency_immediate" to Pair("Immediate Crisis (24-48 Hours)", "انتہائی ہنگامی (24 تا 48 گھنٹے)"),
        "urgency_week" to Pair("This Week (3-7 Days)", "اس ہفتے (3 تا 7 دن)"),
        "urgency_planning" to Pair("Planning Ahead (Formal Grievance)", "انتظامی شکایت (باقاعدہ کارروائی)"),

        // Action Plan Tabs
        "tab_eligibility" to Pair("Eligibility Summary", "اہلیت کا خلاصہ"),
        "tab_checklist" to Pair("Action Checklist", "اقدامی فہرست"),
        "tab_draft_letter" to Pair("Formal Letter", "سرکاری درخواست"),
        "tab_advocacy_script" to Pair("Advocacy Script", "ہیلپ لائن سکرپٹ"),

        // Results actions
        "save_case_btn" to Pair("Save Case to Dashboard", "کیس ڈیش بورڈ میں محفوظ کریں"),
        "case_saved_success" to Pair("Case saved to Active Cases dashboard!", "کیس کامیابی سے محفوظ ہو گیا!"),
        "copy_letter_btn" to Pair("Copy Legal Letter", "درخواست کاپی کریں"),
        "copy_script_btn" to Pair("Copy Script", "سکرپٹ کاپی کریں"),
        "copied_toast" to Pair("Copied to clipboard!", "کلپ بورڈ پر کاپی ہو گیا!"),
        "listen_tts_btn" to Pair("Listen to Script (Audio)", "سکرپٹ سنیں (آڈیو)"),
        "stop_tts_btn" to Pair("Stop Audio", "آواز بند کریں"),
        "tts_playing" to Pair("Playing script...", "سکرپٹ سنایا جا رہا ہے..."),

        // Active Cases
        "cases_heading" to Pair("Citizen Case Tracking Dashboard", "محفوظ شدہ کیسز کی ٹریکنگ ڈیش بورڈ"),
        "cases_subheading" to Pair("Track your formal complaints, applications, and hearing progress.", "اپنی شکایات، نادرا/بی آئی ایس پی درخواستوں اور پیش رفت کی نگرانی کریں۔"),
        "empty_cases" to Pair("No active cases saved yet. Generate an action plan on Home and click 'Save Case'.", "ابھی تک کوئی کیس محفوظ نہیں ہوا۔ ہوم سکرین سے پلان بنا کر محفوظ کریں۔"),
        "status_pending" to Pair("Pending Review", "زیرِ جائزہ"),
        "status_in_progress" to Pair("In Progress", "زیرِ کارروائی"),
        "status_filed" to Pair("Filed with Authority", "جمع شدہ"),
        "status_resolved" to Pair("Resolved & Relieved", "حل شدہ"),

        // Document Vault
        "vault_heading" to Pair("Secure Citizen Document Vault", "محفوظ شہری دستاویزات والٹ"),
        "vault_subheading" to Pair("Encrypted local storage for CNIC, FRC, BISP letters, and utility bills.", "شناختی کارڈ، ایف آر سی اور بجلی کے بلوں کا محفوظ آف لائن ریکارڈ۔"),
        "upload_doc_btn" to Pair("Upload / Scan Document", "دستاویز شامل کریں"),
        "empty_vault" to Pair("No documents stored in vault. Add photos of your CNIC, BISP slips, or rent deeds.", "والٹ خالی ہے۔ شناختی کارڈ، بی آئی ایس پی سلپ یا کرایہ نامہ شامل کریں۔"),
        "doc_referenced_badge" to Pair("Referenced in Action Plans", "حکمتِ عملی میں استعمال شدہ"),

        // Offline Checklist
        "offline_heading" to Pair("Official Offline Procedures & Checklists", "آف لائن قانونی تقاضے و رہنمائی"),
        "offline_subheading" to Pair("Step-by-step verified procedures for NADRA, BISP, Sehat Card, and Wafaqi Mohtasib.", "انٹرنیٹ کے بغیر نادرا، بے نظیر، صحت کارڈ اور محتسب کی مستند معلومات۔"),

        // Errors & Retries
        "error_general" to Pair(
            "Our AI caseworker encountered an issue. You can retry or access saved cases offline.",
            "اے آئی سسٹم سے رابطہ نہیں ہو سکا۔ آپ دوبارہ کوشش کر سکتے ہیں یا آف لائن گائیڈز دیکھ سکتے ہیں۔"
        ),
        "retry_btn" to Pair("Retry Generation", "دوبارہ کوشش کریں"),
        "offline_mode_badge" to Pair("Offline Mode Available", "آف لائن موڈ دستیاب ہے")
    )
}
