plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bancu.ungdungcuatoi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bancu.ungdungcuatoi"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // Dành cho Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // (Tùy chọn) Dành cho Đăng nhập Google
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // (Tùy chọn) Dành cho Đăng nhập Facebook
    implementation("com.facebook.android:facebook-login:latest.release")
}