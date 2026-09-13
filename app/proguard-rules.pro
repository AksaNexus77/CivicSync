# Production ProGuard / R8 Rules for CivicSync Global

# Preserve line numbers and source file attributes for actionable crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve standard annotations and inner classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# -------------------------------------------------------------
# CivicSync Domain & Serialization Models
# -------------------------------------------------------------
# Prevent R8 from obfuscating or stripping JSON serializable entities
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
    <methods>;
}
-keepclassmembers class * {
    public static final *** Companion;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve CivicSync domain models, local entities, and UI state models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.ui.CivicSyncUiState { *; }
-keep class com.example.ui.PlanTab { *; }
-keep class com.example.ui.NavigationDest { *; }
-keep class com.example.util.AppLanguage { *; }

# -------------------------------------------------------------
# Google Gemini API & Firebase AI Rules
# -------------------------------------------------------------
-keep class com.google.firebase.ai.** { *; }
-dontwarn com.google.firebase.ai.**

# -------------------------------------------------------------
# Retrofit, OkHttp, Moshi Network Stack
# -------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**

-keep class com.squareup.moshi.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# -------------------------------------------------------------
# AndroidX Room Database & SQLite
# -------------------------------------------------------------
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# -------------------------------------------------------------
# Dagger Hilt & Dependency Injection
# -------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# -------------------------------------------------------------
# Coroutines & ProfileInstaller
# -------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keep class androidx.profileinstaller.** { *; }

