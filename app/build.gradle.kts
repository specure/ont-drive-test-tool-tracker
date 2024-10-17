plugins {
    alias(libs.plugins.signaltracker.android.application.compose)
    alias(libs.plugins.signaltracker.jvm.ktor)
}

android {
    namespace = "com.specure.signaltracker"

    defaultConfig {
        versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Disable R8 for release builds
            isShrinkResources = true
        }
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Coil
    implementation(libs.coil.compose)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Crypto
    implementation(libs.androidx.security.crypto.ktx)

    // Koin
    implementation(libs.bundles.koin)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Location
    implementation(libs.google.android.gms.play.services.location)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Timber
    implementation(libs.timber)

    implementation(projects.core.presentation.designsystem)
    implementation(projects.core.presentation.ui)

    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.connectivity.domain)
    implementation(projects.connectivity.presentation)

    implementation(projects.intercom.domain)
    implementation(projects.intercom.data)

    implementation(projects.permissions.presentation)
    implementation(projects.track.domain)
    implementation(projects.track.data)

    implementation(projects.track.presentation)
    implementation(projects.track.domain)
    implementation(projects.track.data)
    implementation(projects.track.location)

    implementation(platform(libs.firebase))

}

//
//tasks.register<Copy>("copyGoogleServices") {
//    val destinationPath = "${project.projectDir}/src/debug/"
//    from("${project.rootDir}/private/google-services.json") // Source file in 'private' folder
//    into("${project.projectDir}") // Destination: app module's debug source set
//}
//
//
//tasks.named("preBuild") {
//    dependsOn("copyGoogleServices")
//}