plugins {
    // Android
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false

    // Compose & Kotlin
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    // Google Services (Firebase)
    alias(libs.plugins.google.services) apply false

    // Serialization (JSON) - NEEDED for Ktor
    alias(libs.plugins.kotlinSerialization) apply false

    // Crashlytics (Firebase)
    alias(libs.plugins.firebase.crashlytics) apply false
}