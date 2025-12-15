import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary) // It is a Library, not an App
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)  // Needed for Room
    alias(libs.plugins.room) // Needed for Room
    alias(libs.plugins.buildkonfig) // Storing secrets in local.properties
}

// --- 1. SECURELY READ THE KEY ---
val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(localFile.inputStream())
}

// Try to get from local.properties, fallback to System Environment (good for CI/CD)
val webClientId: String = localProperties.getProperty("WEB_CLIENT_ID")
    ?: System.getenv("WEB_CLIENT_ID")
    ?: "MISSING_API_KEY"

// --- 2. GENERATE THE CONFIG ---
buildkonfig {
    packageName = "com.eeseka.shelflife"
    objectName = "AppConfig"
    exposeObjectWithName = "AppConfig"

    defaultConfigs {
        // BuildKonfig handles the quotes for Strings automatically
        buildConfigField(STRING, "WEB_CLIENT_ID", webClientId)
    }
    // Debug builds
    defaultConfigs("debug") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
    }

    // Release builds
    defaultConfigs("release") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
    }
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

            // --- Firebase ---
            implementation(libs.firebase.auth)

            // --- Capabilities (ShelfLife Stack) ---
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.peekaboo.ui)
            implementation(libs.kmp.notifier)

            // --- Data & State ---
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.datetime)
            implementation(libs.touchlab.kermit)

            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

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
