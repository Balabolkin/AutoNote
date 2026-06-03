plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

android {
    namespace = "ru.diploma.autocareledger"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.diploma.autocareledger"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "MAPKIT_API_KEY",
            "\"${localProperties.getProperty("MAPKIT_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "YANDEX_API_KEY",
            "\"${localProperties.getProperty("YANDEX_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "YANDEX_FOLDER_ID",
            "\"${localProperties.getProperty("YANDEX_FOLDER_ID", "")}\""
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "autonotepad"
            keyAlias = "my-key-alias"
            keyPassword = "autonotepad"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.tesseract4android)
    implementation(libs.yandex.mapkit)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    ksp(libs.androidx.room.compiler)
}
