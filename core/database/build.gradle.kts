plugins {
    alias(libs.plugins.signaltracker.android.library)
    alias(libs.plugins.signaltracker.android.room)
}

android {
    namespace = "com.specure.core.database"
}

dependencies {

    implementation(libs.org.mongodb.bson)
    implementation(libs.bundles.koin)
    implementation(libs.csv)
    implementation(libs.kotlin.reflect)
    implementation(libs.timber)

    implementation(projects.core.domain)

}