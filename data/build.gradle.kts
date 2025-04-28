plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
//    alias(libs.plugins.jetbrains.kotlin.jvm)
}

android {
    namespace = "com.example.data" // Altere para o seu pacote correto
    compileSdk = 34

    /*defaultConfig {
        minSdk = 24
    }*/

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)

    // Linphone SDK
    debugImplementation(libs.linphone.android.debug)
    releaseImplementation(libs.linphone.android.release)
    implementation(libs.media)

    // Android Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

}
