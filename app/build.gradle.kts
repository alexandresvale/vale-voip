plugins {
    id("valevoip.android.application")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.valevoip.app"

    defaultConfig {
        applicationId = "com.valevoip.app"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        /*ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"*//*, "x86", "x86_64"*//*))
        }*/

        /*externalNativeBuild {
            cmake {
                arguments += "-DANDROID_STL=c++_static"
            }
        }*/

    }

    val keystorePath = providers.gradleProperty("VALEVOIP_KEYSTORE_FILE")
        .orElse(providers.environmentVariable("VALEVOIP_KEYSTORE_FILE"))
        .orNull
    val keystorePassword = providers.gradleProperty("VALEVOIP_KEYSTORE_PASSWORD")
        .orElse(providers.environmentVariable("VALEVOIP_KEYSTORE_PASSWORD"))
        .orNull
    val keyAliasName = providers.gradleProperty("VALEVOIP_KEY_ALIAS")
        .orElse(providers.environmentVariable("VALEVOIP_KEY_ALIAS"))
        .orNull
    val keyAliasPassword = providers.gradleProperty("VALEVOIP_KEY_PASSWORD")
        .orElse(providers.environmentVariable("VALEVOIP_KEY_PASSWORD"))
        .orNull

    val hasReleaseSigning = !keystorePath.isNullOrBlank() &&
        !keystorePassword.isNullOrBlank() &&
        !keyAliasName.isNullOrBlank() &&
        !keyAliasPassword.isNullOrBlank() &&
        file(keystorePath).exists()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(keystorePath))
                storePassword = keystorePassword
                keyAlias = keyAliasName
                keyPassword = keyAliasPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.AZUL)
        }
    }

    buildFeatures {
        compose = true
    }
    /*composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }*/

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

        /*jniLibs {
            pickFirsts.addAll(
                listOf(
                    "lib/arm64-v8a/libc++_shared.so",
                    "lib/armeabi-v7a/libc++_shared.so",
                    "lib/x86/libc++_shared.so",
                    "lib/x86_64/libc++_shared.so"
                )
            )
        }*/

    }

    /*sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
    }*/

}

dependencies {
    implementation(project(":core:domain")) 
    implementation(project(":core:data"))   
    implementation(project(":core:sip"))
    implementation(project(":core:telecom"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:analytics"))

    // Nossas Features!
    implementation(project(":feature:splash"))
    implementation(project(":feature:login"))
    implementation(project(":feature:home"))
    implementation(project(":feature:call"))
    implementation(project(":feature:dialer"))
    implementation(project(":feature:history"))
    implementation(project(":feature:config"))

    // Kotlin
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material)
    implementation(libs.material.icons.extended)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    // Navigation
    implementation(libs.navigation.compose)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.ui.text.google.fonts)

    // Linphone SDK
//    debugImplementation(libs.linphone.debug)
    implementation(libs.linphone.release)
    implementation(libs.media)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
