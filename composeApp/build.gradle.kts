import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.core.splashscreen)

            // Needed to start the Koin graph in the Application class
            implementation(libs.koin.android)

            // App Check (Firebase)
            implementation(libs.google.firebase.appcheck.playintegrity)
            implementation(libs.google.firebase.appcheck.debug)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.bundles.koin.common)

            // Needed for some app auth initializations
            implementation(libs.auth.google.kmp)
            implementation(libs.firebase.app)

            // --- The Modular Connection ---
            implementation(project(":shared"))
            implementation(project(":di"))
            implementation(project(":navigation"))
        }
    }
}

android {
    namespace = "com.eeseka.shelflife"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("local.properties")
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    defaultConfig {
        applicationId = "com.eeseka.shelflife"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["key.alias"] as String?
            keyPassword = keystoreProperties["key.password"] as String?
            storePassword = keystoreProperties["store.password"] as String?

            // resolving the file path
            val storeFilePath = keystoreProperties["store.file"] as String?
            if (storeFilePath != null) {
                storeFile = rootProject.file(storeFilePath)
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Sign the APK with your key
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}