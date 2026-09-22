plugins {
    id("valevoip.android.library")
    id("valevoip.android.compose")
    id("valevoip.android.hilt")
}

android {
    namespace = "com.valevoip.core.navigation"
}

dependencies {
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
}
