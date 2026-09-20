plugins {
    id("valevoip.android.library")
    id("valevoip.android.compose")
}

android {
    namespace = "com.valevoip.core.designsystem"
}

dependencies {
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)
}