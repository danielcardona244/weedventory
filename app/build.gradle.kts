plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.example.weedventory"
    compileSdk = rootProject.ext.get("compileSdk") as Int

    defaultConfig {
        applicationId = "com.example.weedventory"
        minSdk = rootProject.ext.get("minSdk") as Int
        targetSdk = rootProject.ext.get("targetSdk") as Int
        versionCode = rootProject.ext.get("versionCode") as Int
        versionName = rootProject.ext.get("versionName") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.ext.get("composeVersion") as String
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeVersion = rootProject.ext.get("composeVersion") as String
    val composeMaterial3 = rootProject.ext.get("composeMaterial3") as String
    val navigationCompose = rootProject.ext.get("navigationCompose") as String
    val lifecycleVersion = rootProject.ext.get("lifecycleVersion") as String
    val coreKtxVersion = rootProject.ext.get("coreKtxVersion") as String
    val appcompatVersion = rootProject.ext.get("appcompatVersion") as String
    val roomVersion = rootProject.ext.get("roomVersion") as String
    val workManagerVersion = rootProject.ext.get("workManagerVersion") as String
    val kotlinxSerializationVersion = rootProject.ext.get("kotlinxSerializationVersion") as String
    
    // Core Android
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:$composeMaterial3")
    implementation("androidx.compose.material:material-icons-extended")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.8.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$navigationCompose")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workManagerVersion")

    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
