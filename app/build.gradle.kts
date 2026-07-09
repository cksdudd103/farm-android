plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smartfarm.app"
    // NOTE: compileSdk must match an SDK platform installed locally. This
    // machine has platforms 36 and 37 installed (not 34/35), so compileSdk
    // is set to 36 while targetSdk stays at the requested 34 - targetSdk
    // does not require the platform jar to be installed, only compileSdk
    // does. If you have SDK Platform 34 installed via Android Studio's SDK
    // Manager, you can lower compileSdk to 34 to match targetSdk exactly.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smartfarm.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Backend base URL - points to a shared, open Render deployment by
        // default so multiple users can use the app out-of-the-box without
        // entering a server address on first launch. Users can still override
        // this at runtime from the Settings screen.
        buildConfigField("String", "BASE_URL", "\"https://farm-webapp-rezy.onrender.com/\"")
    }

    // ---------------------------------------------------------------------
    // Signing configs.
    //
    // debug: uses the auto-generated Android debug keystore
    // (~/.android/debug.keystore) that Android Studio/Gradle creates
    // automatically the first time you build - no setup required.
    //
    // release: placeholder that reads credentials from environment
    // variables / gradle.properties so you can generate a signed,
    // installable APK for distribution without committing secrets to the
    // repo. To produce a signed release APK:
    //   1. Generate a keystore:
    //      keytool -genkeypair -v -keystore release-key.jks -alias smartfarm \
    //        -keyalg RSA -keysize 2048 -validity 10000
    //   2. Add to (local, gitignored) gradle.properties or environment vars:
    //      RELEASE_STORE_FILE=release-key.jks
    //      RELEASE_STORE_PASSWORD=your_store_password
    //      RELEASE_KEY_ALIAS=smartfarm
    //      RELEASE_KEY_PASSWORD=your_key_password
    //   3. Run: gradlew assembleRelease
    // If these properties are not set, release build type falls back to
    // the debug signing config so `assembleRelease` still succeeds locally
    // (the resulting APK is only debug-signed and not suitable for the
    // Play Store, but is fully installable on a device via "unknown
    // sources").
    // ---------------------------------------------------------------------
    signingConfigs {
        create("release") {
            val storeFilePath = findProperty("RELEASE_STORE_FILE") as String?
                ?: System.getenv("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = (findProperty("RELEASE_STORE_PASSWORD") as String?)
                    ?: System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = (findProperty("RELEASE_KEY_ALIAS") as String?)
                    ?: System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = (findProperty("RELEASE_KEY_PASSWORD") as String?)
                    ?: System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://farm-webapp-rezy.onrender.com/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"https://farm-webapp-rezy.onrender.com/\"")
            signingConfig = if (project.findProperty("RELEASE_STORE_FILE") != null ||
                System.getenv("RELEASE_STORE_FILE") != null
            ) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.material)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    implementation(libs.coil.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.mpandroidchart)
    implementation(libs.accompanist.permissions)

    debugImplementation(libs.androidx.ui.tooling)
}
