plugins {
    id("valevoip.android.library")
    id("valevoip.android.hilt")
}

android {
    namespace = "com.valevoip.core.analytics"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    api(libs.timber)
}
