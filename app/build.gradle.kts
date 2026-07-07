import com.android.ddmlib.Log

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.example.productshop"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    ndkVersion = "27.2.12479018"
    defaultConfig {
        applicationId = "com.example.productshop"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // buildConfigField("String", "SYSTEM_PASSWORD", "\"Admin@System2024\"")
        ndk {
            // No physical Android device is x86_64; this only affects emulators
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://boozy-supply-ripping.ngrok-free.dev/\"")
        }
        create("qa") {
            dimension = "environment"
            versionNameSuffix = "-qa"
            buildConfigField("String", "BASE_URL", "\"https://qa.insuretechguard.com/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.insuretechguard.com/\"")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)
    implementation(libs.javax.mail)
    implementation(libs.javax.activation)
    implementation(platform("com.google.firebase:firebase-bom:34.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.crashlytics)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

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