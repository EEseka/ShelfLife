import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary) // It is a Library, not an App
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)  // Needed for Room
    alias(libs.plugins.room) // Needed for Room
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)

            // CameraX Fix (The 16KB Page Size issue logic lives where the camera lib is)
            implementation(libs.androidx.camera.core)
        }
        commonMain.dependencies {
            // --- UI Essentials ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // -- Permission handling ---
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.notifications)

            // -- Logging --
            implementation(libs.touchlab.kermit)

            // --- Architecture ---
            implementation(libs.bundles.koin.common)
            implementation(libs.bundles.ktor.common)

            // --- Capabilities (ShelfLife Stack) ---
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.peekaboo.ui)
            implementation(libs.kmp.notifier)

            // --- Data & State ---
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.touchlab.kermit)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.material3.adaptive)

            // --- Database (Room) ---
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.eeseka.shelflife.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Room Schema Setup
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP Configuration for Room (Must be in the module that uses Room)
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    debugImplementation(compose.uiTooling)
}