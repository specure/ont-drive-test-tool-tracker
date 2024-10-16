plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.specure.track.data"
}

dependencies {

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.google.android.gms.play.services.location)
    implementation(libs.androidx.work)
    implementation(libs.koin.android.workmanager)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(projects.track.domain)
    implementation(projects.core.database)
    implementation(libs.androidx.preference.ktx)
}