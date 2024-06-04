plugins {
    alias(libs.plugins.signaltracker.android.feature.ui)
    alias(libs.plugins.mapsplatform.secrets.plugin)
}

android {
    namespace = "com.cadrikmdev.track.presentation"
}

dependencies {

    implementation(libs.coil.compose)
    implementation(libs.google.maps.android.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(projects.track.domain)
    implementation(projects.permissions.domain)
    implementation(projects.permissions.presentation)
    implementation(projects.core.presentation.service)
    implementation(projects.core.connectivity)
}