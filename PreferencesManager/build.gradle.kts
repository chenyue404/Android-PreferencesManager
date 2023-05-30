import com.android.build.gradle.internal.tasks.factory.dependsOn

// https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

android {
    compileSdk = 33

    defaultConfig {
        namespace = "fr.simon.marquis.preferencesmanager"
        applicationId = "fr.simon.marquis.preferencesmanager"
        minSdk = 24
        targetSdk = 33

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
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose#declaring_dependencies
        kotlinCompilerExtensionVersion = "1.4.7"
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
        project.tasks.preBuild.dependsOn("ktlintCheck")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")

    // https://developer.android.com/jetpack/androidx/releases/compose
    val composeBom = platform("androidx.compose:compose-bom:2023.05.01")
    implementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.7.1")
    implementation("androidx.compose.material3:material3:1.2.0-alpha01")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // https://mvnrepository.com/artifact/com.google.accompanist/accompanist-systemuicontroller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.2-alpha")

    // https://mvnrepository.com/artifact/de.charlex.compose/html-text
    implementation("de.charlex.compose:html-text:1.4.1")

    // https://mvnrepository.com/artifact/io.coil-kt/coil-compose
    implementation("io.coil-kt:coil-compose:2.4.0")

    // https://github.com/topjohnwu/libsu/releases
    implementation("com.github.topjohnwu.libsu:core:5.1.0")

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
