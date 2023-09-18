import com.android.build.gradle.internal.tasks.factory.dependsOn

// https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

android {
    compileSdk = 34

    defaultConfig {
        namespace = "fr.simon.marquis.preferencesmanager"
        applicationId = "fr.simon.marquis.preferencesmanager"

        minSdk = 24
        targetSdk = 34

        versionCode = 192
        versionName = "1.9.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose#declaring_dependencies
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    tasks {
        project.tasks.preBuild.dependsOn("ktlintFormat")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")

    // https://developer.android.com/jetpack/androidx/releases/compose
    val composeBom = platform("androidx.compose:compose-bom:2023.09.00")
    implementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // https://mvnrepository.com/artifact/androidx.activity/activity-compose
    implementation("androidx.activity:activity-compose:1.8.0-beta01")
    // https://mvnrepository.com/artifact/androidx.core/core-ktx
    implementation("androidx.core:core-ktx:1.12.0")
    // https://mvnrepository.com/artifact/androidx.core/core-splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")
    // https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-runtime-compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0-alpha02")
    // https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-runtime-ktx
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0-alpha02")
    // https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-viewmodel-compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0-alpha02")
    // https://mvnrepository.com/artifact/androidx.preference/preference-ktx
    implementation("androidx.preference:preference-ktx:1.2.1")

    // https://jitpack.io/#Hospes/headed-lazy-grid
    implementation("com.github.Hospes:headed-lazy-grid:0.9.0")

    // https://mvnrepository.com/artifact/de.charlex.compose/html-text
    implementation("de.charlex.compose:html-text:1.5.0")

    // https://mvnrepository.com/artifact/com.github.skydoves/landscapist-coil
    implementation("com.github.skydoves:landscapist-bom:2.2.8")
    implementation("com.github.skydoves:landscapist-coil")

    // https://github.com/topjohnwu/libsu/releases
    implementation("com.github.topjohnwu.libsu:core:5.2.1")

    // https://mvnrepository.com/artifact/com.jakewharton.timber/timber
    implementation("com.jakewharton.timber:timber:5.0.1")
}

/**
 * Ktlint gradle
 *  Usages:
 *      gradlew ktlintCheck
 *      gradlew ktlintFormat
 */
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
    outputToConsole.set(true)
}
