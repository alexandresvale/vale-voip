plugins {
    id("valevoip.android.feature")
}
android {
    namespace = "com.valevoip.feature.call"
}
dependencies {
    implementation(project(":core:domain"))
}