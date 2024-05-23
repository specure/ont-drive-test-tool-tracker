plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.cadrikmdev.core.mobileNetwork"
}

dependencies {

    implementation(libs.bundles.koin)
    implementation(libs.netmonster.core)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(projects.core.database)
}