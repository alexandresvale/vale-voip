plugins {
    id("valevoip.android.library")
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.valevoip.core.data"

    /*compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }*/
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.core)

    // Linphone SDK
//    debugImplementation(libs.linphone.debug)
    implementation(libs.linphone.release)
    implementation(libs.media)

    // Android Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Security
    implementation(libs.androidx.datastore)
    implementation(libs.tink.android)
    implementation(libs.kotlinx.serialization.json)

    // Timber
    implementation(libs.timber)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.hilt.android.testing)

}
