plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.specure.track.location"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.koin)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.google.android.gms.play.services.location)

    implementation(projects.core.domain)
    implementation(projects.track.domain)
}