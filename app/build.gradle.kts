plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "nl.ovfietsbeschikbaarheid"
    compileSdk = 34

    defaultConfig {
        applicationId = "nl.ovfietsbeschikbaarheid"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.0"

        // Only include resources for supported languages
        resourceConfigurations += listOf("nl", "en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
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
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.google.maps.compose)
    implementation(libs.csv)
    implementation(libs.timber)

    implementation(libs.compass.geocoder)
    implementation(libs.compass.geocoder.mobile)
    implementation(libs.compass.geolocation)
    implementation(libs.compass.geolocation.mobile)
    implementation(libs.compass.permissions.mobile)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.serialization.json)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.logging)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test.junit4)
    // No need for kluent-android: the only difference is stuff about functions with spaces in them, but Android has support for that for ages now
    // https://github.com/MarkusAmshove/Kluent/pull/58
    testImplementation(libs.kluent)
    // To get JUnit errors from kotlin.test, to e.g. enable diff windows in failure messages
    testImplementation(libs.kotlin.test.junit)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}