package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SavedCaseEntity::class,
        VaultDocumentEntity::class,
        OfflineChecklistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CivicSyncDatabase : RoomDatabase() {

    abstract fun savedCaseDao(): SavedCaseDao
    abstract fun vaultDocumentDao(): VaultDocumentDao
    abstract fun offlineChecklistDao(): OfflineChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: CivicSyncDatabase? = null

        fun getDatabase(context: Context): CivicSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CivicSyncDatabase::class.java,
                    "civic_sync_pakistan.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default offline Pakistani civic checklists
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.offlineChecklistDao()?.insertAll(seedOfflineChecklist())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun seedOfflineChecklist(): List<OfflineChecklistEntity> = listOf(
            OfflineChecklistEntity(
                id = "off_nadra_1",
                category = "NADRA Civil Identity",
                categoryUr = "نادرا رجسٹریشن",
                title = "CNIC Biometric & Renewal Requirements",
                titleUr = "شناختی کارڈ بائیو میٹرک اور تجدید کے ضروری تقاضے",
                description = "Carry original old CNIC, verified B-Form / CRC of children, Family Registration Certificate (FRC), and electricity utility bill to the nearest NADRA Registration Center (NRC). If senior citizen prints fail, request Form 102 Manual Verification.",
                descriptionUr = "قریبی نادرا سنٹر پر اصل شناختی کارڈ، بچوں کا ب فارم، اور بجلی کا بل لے کر جائیں۔ اگر فنگر پرنٹس نہ آئیں تو فارم 102 مینوئل تصدیق کی درخواست کریں۔",
                helpline = "1777 (Mobile) / 051-111-786-100",
                isCompleted = false
            ),
            OfflineChecklistEntity(
                id = "off_bisp_2",
                category = "BISP Social Protection",
                categoryUr = "بے نظیر انکم سپورٹ پروگرام",
                title = "8171 SMS & Tehsil Dynamic Registry",
                titleUr = "8171 ایس ایم ایس اور تحصیل ڈائنامک رجسٹری",
                description = "Send your 13-digit CNIC (without dashes) to 8171 to verify PMT poverty score and Kafalat eligibility. If payment blocked due to biometrics, visit the BISP Tehsil Office with CNIC to request BISP Biometric Alternative Payment Authorization (BAPA).",
                descriptionUr = "اپنا 13 ہندسوں کا شناختی کارڈ نمبر 8171 پر میسج کریں۔ رقم بلاک ہونے کی صورت میں بی آئی ایس پی تحصیل دفتر جا کر بائیو میٹرک متبادل ادائیگی فارم جمع کروائیں۔",
                helpline = "0800-26477 (Toll-Free)",
                isCompleted = false
            ),
            OfflineChecklistEntity(
                id = "off_sehat_3",
                category = "Sehat Sahulat Program",
                categoryUr = "صحت سہولت پروگرام / صحت کارڈ",
                title = "Hospital Indoor Admission & Treatment",
                titleUr = "ہسپتال میں داخلہ اور مفت علاج کا طریقہ کار",
                description = "Present original CNIC at the Sehat Sahulat facilitation desk inside any empaneled public or private hospital. Indoor hospitalization up to Rs. 1,000,000 per family per year is 100% free of charge under government cover.",
                descriptionUr = "کسی بھی پینل ہسپتال کے صحت سہولت کاؤنٹر پر اصل شناختی کارڈ دکھائیں۔ سالانہ 10 لاکھ روپے تک کا داخلہ اور سرجری بالکل مفت ہے۔",
                helpline = "0800-09009 (Toll-Free)",
                isCompleted = false
            ),
            OfflineChecklistEntity(
                id = "off_mohtasib_4",
                category = "Wafaqi Mohtasib (Ombudsman)",
                categoryUr = "وفاقی محتسب سیکرٹریٹ",
                title = "Free Maladministration Redressal Grievance",
                titleUr = "سرکاری دفاتر کی ناانصافی کے خلاف مفت شکایت",
                description = "If NADRA, BISP, EOBI, WAPDA, or Sui Gas causes unjustified delays or rejects lawful rights, file a complaint on plain paper or via Helpline 1055. Decisions are issued within 60 days without court fees or legal expense.",
                descriptionUr = "اگر نادرا، بی آئی ایس پی، ای او بی آئی یا واپڈا غیر ضروری تاخیر کرے تو ہیلپ لائن 1055 یا سادہ کاغذ پر مفت شکایت درج کروائیں۔ 60 دن میں فیصلہ ہو گا۔",
                helpline = "1055 (Free Helpline)",
                isCompleted = false
            ),
            OfflineChecklistEntity(
                id = "off_baitulmal_5",
                category = "Pakistan Bait-ul-Mal (PBM)",
                categoryUr = "پاکستان بیت المال",
                title = "Emergency Medical Grant & Zakat Application",
                titleUr = "ہنگامی طبی گرانٹ اور امداد کی درخواست",
                description = "Obtain hospital prescription estimate, photocopy of CNIC, and local Councilor/Mustahiq verification certificate. Submit to the District PBM Office for immediate medical grant or educational scholarship.",
                descriptionUr = "ہسپتال کے اخراجات کا تخمینہ اور شناختی کارڈ کی کاپی ڈسٹرکٹ بیت المال آفس جمع کروا کر ہنگامی طبی فنڈ حاصل کریں۔",
                helpline = "0800-66666",
                isCompleted = false
            )
        )
    }
}
