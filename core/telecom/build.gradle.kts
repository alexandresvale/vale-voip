plugins {
    id("valevoip.android.library")
    id("valevoip.android.hilt")
}

android {
    namespace = "com.valevoip.core.telecom"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    
    implementation(libs.androidx.core.ktx)

    implementation(libs.appcompat)
    /*implementation(libs.material)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)*/

    /*// Linphone SDK
    debugImplementation(libs.linphone.android.debug)
    releaseImplementation(libs.linphone.android.release)
    implementation(libs.media)*/
}