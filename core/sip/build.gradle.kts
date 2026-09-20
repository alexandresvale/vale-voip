plugins {
    id("valevoip.android.library")
    id("valevoip.android.hilt")
}

android {
    namespace = "com.valevoip.core.sip"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.linphone.release)
    implementation(libs.kotlinx.coroutines.core)
}