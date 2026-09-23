plugins {
    id("valevoip.android.library")
    id("valevoip.android.compose")
    id("valevoip.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.valevoip.core.navigation"
}

dependencies {
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
