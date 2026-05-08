import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    kotlin("plugin.serialization") version "2.0.0"
}

compose.resources {
    packageOfResClass = "nl.ovfietsbeschikbaarheid.resources"
}

kotlin {
    android {
        namespace = "nl.ovfietsbeschikbaarheid.shared"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTest { isIncludeAndroidResources = true }
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // Dummy task to let CodeQL / Analyze (java-kotlin) complete
    tasks.register("testClasses")

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
            implementation(libs.play.review.ktx)

            implementation(libs.google.maps.compose)
            implementation(libs.kermit)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)

            implementation(libs.ktor.client.android)

            implementation(libs.play.services.location)
            implementation(libs.play.services.coroutines)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.ios)
            implementation(libs.compass.geolocation.mobile)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.material3)
            implementation(libs.material.icons.extended)

            implementation(libs.compose.shimmer)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)
            implementation(libs.navigation3.ui)
            implementation(libs.adaptive.layout)

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
        val androidHostTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.koin.test.junit4)
                // To get JUnit errors from kotlin.test, to e.g. enable diff windows in failure messages
                implementation(libs.kotlin.test.junit)
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.junit)
                implementation(libs.androidx.espresso.core)
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.ui.test.junit4)
            }
        }
    }
}
