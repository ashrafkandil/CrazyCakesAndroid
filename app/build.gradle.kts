import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun configValue(name: String, fallback: String): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: fallback).trim()

fun quoted(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.marwacrazycakes.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marwacrazycakes.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "GEMINI_API_KEY", quoted(configValue("GEMINI_API_KEY", "")))
        buildConfigField("String", "GEMINI_MODEL", quoted(configValue("GEMINI_MODEL", "")))
        buildConfigField(
            "String",
            "GEMINI_API_BASE_URL",
            quoted(configValue("GEMINI_API_BASE_URL", "https://generativelanguage.googleapis.com")),
        )
        buildConfigField(
            "String",
            "MOBILE_CONTENT_URL",
            quoted(
                configValue(
                    "MOBILE_CONTENT_URL",
                    "https://ashrafkandil.github.io/CrazyCakesMobile/mobile-content",
                ),
            ),
        )
    }

    androidResources {
        localeFilters += "en"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.media3:media3-exoplayer:1.9.4")
    implementation("androidx.media3:media3-ui:1.9.4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
