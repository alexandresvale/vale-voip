plugins {
    id("valevoip.android.library")
    id("valevoip.android.compose")
}

android {
    namespace = "com.valevoip.core.navigation"
}

dependencies {
    implementation(libs.navigation.compose)
}
