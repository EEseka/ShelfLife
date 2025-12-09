plugins {
    // Android
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false

    // Compose & Kotlin
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    // Serialization (JSON) - NEEDED for Ktor
    alias(libs.plugins.kotlinSerialization) apply false

    // Database (Room) - NEEDED for shared
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}