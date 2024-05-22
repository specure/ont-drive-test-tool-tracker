plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.cadrikmdev.core.mobile_network"
}

dependencies {

    implementation(libs.bundles.koin)

    implementation(projects.core.domain)
}