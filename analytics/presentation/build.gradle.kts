plugins {
    alias(libs.plugins.runique.android.feature.ui)
}

android {
    namespace = "com.cadrikmdev.analytics.presentation"
}

dependencies {

    implementation(projects.analytics.domain)

}