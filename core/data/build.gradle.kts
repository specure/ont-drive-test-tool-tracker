plugins {
    alias(libs.plugins.signaltracker.android.library)
    alias(libs.plugins.signaltracker.jvm.ktor)
}

android {
    namespace = "com.specure.core.data"
}

dependencies {
    implementation(libs.timber)
    implementation(libs.bundles.koin)

    implementation(projects.core.domain)
    implementation(projects.core.database)
}