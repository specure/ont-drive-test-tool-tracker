plugins {
    alias(libs.plugins.signaltracker.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.core.domain)
}