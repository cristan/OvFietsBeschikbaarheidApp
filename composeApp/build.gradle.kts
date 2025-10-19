
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    kotlin("plugin.serialization") version "2.0.0"
}

compose.resources {
    packageOfResClass = "nl.ovfietsbeschikbaarheid.resources"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
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
            implementation(libs.kotlinx.datetime)

            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.play.review.ktx)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.androidx.material3)


            implementation(libs.google.maps.compose)
            implementation(libs.kermit)
            implementation(libs.compose.shimmer)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.ktor.client.android)

            implementation(libs.play.services.location)
            implementation(libs.play.services.coroutines)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.material.icons.extended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.compose.shimmer)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.kermit)

            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.client.logging)

            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)
            implementation(libs.compass.permissions.mobile)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.androidx.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.koin.test.junit4)
            // To get JUnit errors from kotlin.test, to e.g. enable diff windows in failure messages
            implementation(libs.kotlin.test.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.ui.test.junit4)
        }
    }
}

android {
    namespace = "nl.ovfietsbeschikbaarheid"
    compileSdk = 36

    defaultConfig {
        applicationId = "nl.ovfietsbeschikbaarheid"
        minSdk = 26
        targetSdk = 36
        versionCode = 23
        versionName = "3.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        // Keeps language resources for only the locales specified below.
        @Suppress("UnstableApiUsage")
        localeFilters += listOf("nl", "en")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

