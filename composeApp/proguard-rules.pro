# ==============================================================================
# 🛡️ SHELFLIFE PRODUCTION R8 RULES (v1.0)
# ==============================================================================

# 1. FIX NAV BAR INDICATOR (Keep Navigation Classes)
# R8 was renaming 'Screen.Pantry', breaking the 'isSelected' check in MainScreen.kt.
-keep class com.eeseka.shelflife.shared.navigation.** { *; }

# 2. Fix Ktor/Firebase Build Warnings
-dontwarn java.lang.management.**
-dontwarn com.google.firebase.ktx.**
-dontwarn com.google.firebase.auth.ktx.**

# ------------------------------------------------------------------------------
# 3. KOTLIN SERIALIZATION & DATA
# ------------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep DTOs and Domain Models
-keep class com.eeseka.shelflife.shared.data.dto.** { *; }
-keep class com.eeseka.shelflife.shared.domain.** { *; }

# ------------------------------------------------------------------------------
# 4. MATERIAL 3 & COMPOSE
# ------------------------------------------------------------------------------
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.** { *; }

# ------------------------------------------------------------------------------
# 5. KOIN
# ------------------------------------------------------------------------------
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class com.eeseka.shelflife.di.** { *; }

# ------------------------------------------------------------------------------
# 6. ROOM DATABASE
# ------------------------------------------------------------------------------
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase {
    <init>();
    static ** *;
}
-keep class com.eeseka.shelflife.shared.data.database.local.** { *; }
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 7. FIREBASE, KTOR, COROUTINES
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class io.ktor.** { *; }
-keep class io.ktor.client.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}