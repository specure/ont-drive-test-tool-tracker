plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.cadrikmdev.core.mobile_network"
}

dependencies {

    implementation(libs.bundles.koin)
    implementation(libs.netmonster.core)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(project(":core:database"))
}