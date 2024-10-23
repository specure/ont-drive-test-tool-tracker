plugins {
    alias(libs.plugins.signaltracker.android.library)
    alias(libs.plugins.signaltracker.jvm.ktor)
}

android {
    namespace = "com.specure.updater.data"
}

dependencies {

    implementation(projects.updater.domain)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.bundles.koin)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}