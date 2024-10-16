plugins {
    alias(libs.plugins.signaltracker.android.library.compose)
}

android {
    namespace = "com.specure.core.presentation.service"
}

dependencies {

    implementation(libs.timber)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.google.android.gms.play.services.location)
    implementation(project(":core:domain"))
    debugImplementation(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.material3)
}