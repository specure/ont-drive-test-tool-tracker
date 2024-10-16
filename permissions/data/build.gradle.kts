plugins {
    alias(libs.plugins.signaltracker.android.library)
}

android {
    namespace = "com.specure.permissions.data"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.permissions.domain)
}