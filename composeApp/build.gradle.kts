import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
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
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.androidx.compose.ui.tooling.preview)
            // CameraX core library
            implementation(libs.androidx.camera.core)
            // CameraX Camera2 extensions
            implementation(libs.androidx.camera.camera2)
            // CameraX Lifecycle library
            implementation(libs.androidx.camera.lifecycle)
            // CameraX View class
            implementation(libs.androidx.camera.view)

            // ML Kit Barcode Scanning
            implementation("com.google.mlkit:barcode-scanning:17.2.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)

            implementation(libs.bundles.ktor)

            implementation(libs.kotlinx.serialization.json)

            //permission handling
            api("dev.icerock.moko:permissions:0.19.1")
            api("dev.icerock.moko:permissions-compose:0.19.1")
            implementation("dev.icerock.moko:permissions-camera:0.19.1")
            implementation("dev.icerock.moko:permissions-gallery:0.19.1")

            //compose-navigation
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta05")

            //coil
            implementation("media.kamel:kamel-image:0.9.5")

        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

            //camera
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.identify"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.identify"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

