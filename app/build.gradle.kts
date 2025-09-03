plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Added this line
}

android {
    namespace = "com.example.photo_organizer" // Corrected: use '=' for assignment
    compileSdk = 36 // Corrected: use '=' for assignment

    defaultConfig {
        applicationId = "com.example.photo_organizer" // Corrected: use '=' for assignment
        minSdk = 26 // Corrected: use '=' for assignment
        targetSdk = 34 // Corrected: use '=' for assignment
        versionCode = 1 // Corrected: use '=' for assignment
        versionName = "1.0" // Corrected: use '=' for assignment
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true // Corrected: use '=' for assignment
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Corrected: use '=' for assignment
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Or your desired Java version
        targetCompatibility = JavaVersion.VERSION_1_8 // Or your desired Java version
    }
    kotlinOptions {
        jvmTarget = "1.8" // Or your desired Java version, matching compileOptions
    }
    kotlin {
        jvmToolchain(17) // Specify the JDK version you want to use (e.g., 17)
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-core:1.6.8") // Or the latest version
    implementation("androidx.compose.material:material-icons-extended:1.6.8") // Or the latest version
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8") // Or the latest stable version
    implementation(libs.androidx.core.ktx) // Use version catalog alias
    implementation(libs.androidx.activity.compose) // Use version catalog alias
    implementation(libs.androidx.ui) // Use version catalog alias
    implementation(libs.androidx.material3) // Use Material3 alias if you intend to use it
    implementation(platform(libs.androidx.compose.bom)) // Ensure this line is present
    implementation(libs.androidx.material3)
    // implementation "androidx.compose.material:material:1.6.0" // This is Material 2, consider libs.androidx.material3
    implementation(libs.androidx.lifecycle.runtime.ktx) // Use version catalog alias
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2") // No alias found for this in your toml, direct declaration is fine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // No alias found for this in your toml

    // Optional: accompanist permissions helper
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta") // No alias found for this in your toml
}
