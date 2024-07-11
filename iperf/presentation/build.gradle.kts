plugins {
    alias(libs.plugins.signaltracker.android.feature.ui)
    alias(libs.plugins.mapsplatform.secrets.plugin)
}

android {
    namespace = "com.cadrikmdev.iperf.presentation"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(projects.iperf.domain)
    implementation(projects.core.domain)
    implementation(projects.iperfJniUpload.iperf)
    implementation(projects.iperfJniDownload.iperf)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}