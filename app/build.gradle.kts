plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt) apply true
    alias(libs.plugins.google.dagger.hilt)
}

android {
    namespace = "com.example.stushare"
    compileSdk = 36 // <-- 1. ĐÃ SỬA LỖI CÚ PHÁP

    defaultConfig {
        applicationId = "com.example.stushare"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/NOTICE.md")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/io.netty.versions.properties")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}


dependencies {
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(libs.androidx.compose.material3.windowSizeClass)
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    // Navigation
    implementation(libs.navigation.compose)
    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // Compose BOM & UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Icons
    implementation(libs.material.icons.extended)
    // Coil (Tải ảnh)
    implementation(libs.coil.compose)
    // Room (Database)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    // Retrofit (Network)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    // Moshi (JSON)
    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)
    // (Các thư viện khác)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.material.core)


    // ===============================================
    //               UNIT TESTING SETUP
    // ===============================================

    // Testing Nền tảng
    testImplementation(libs.junit)
    // ⚠️ BỔ SUNG: Kotlin Test cho assertFailsWith
    testImplementation(libs.kotlin.test.junit)

    // Testing cho Coroutines/Flow (Cho runTest)
    testImplementation(libs.kotlinx.coroutines.test)

    // Mocking Library (Cho mockk, coEvery)
    testImplementation(libs.mockk)

    // Testing cho LiveData/Architecture (Cho assert methods)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.arch.core.testing)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}