import java.util.Properties

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

fun gitCommitCount(): Int =
    Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
        .inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 1

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.play.publisher)
}

android {
    namespace = "org.sathyasaieire.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.shayshank.saiireland"
        minSdk = 24
        targetSdk = 36
        versionCode = 21
        versionName = "1.21"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Credentials come from the environment first (this is how CI supplies them —
        // see .github/workflows/ci.yml's `release` job), falling back to local.properties
        // for local release builds. No hardcoded defaults: a release build should fail
        // loudly if it isn't configured, rather than silently sign with a shared password.
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: localProps.getProperty("STORE_FILE")
            if (keystorePath != null) storeFile = file(keystorePath)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: localProps.getProperty("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS") ?: localProps.getProperty("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD") ?: localProps.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

play {
    serviceAccountCredentials.set(rootProject.file("play-service-account.json"))
    track.set("alpha")
    defaultToAppBundles.set(true)
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.androidx.compose.bom))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation + Hilt nav
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Google Sign-In
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity.googleid)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Browser (Custom Tabs)
    implementation(libs.androidx.browser)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
