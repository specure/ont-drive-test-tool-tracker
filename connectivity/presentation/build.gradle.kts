plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.specure.connectivity.presentation"
}

dependencies {

    implementation(libs.bundles.koin)
    implementation(libs.netmonster.core)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.connectivity.domain)
}