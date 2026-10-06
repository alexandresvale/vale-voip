plugins {
    id("valevoip.android.feature")
}
android {
    namespace = "com.valevoip.feature.home"
}

dependencies {
    implementation(project(":core:telecom"))
}
