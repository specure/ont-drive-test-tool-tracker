plugins {
    alias(libs.plugins.signaltracker.jvm.library)
    alias(libs.plugins.signaltracker.jvm.ktor)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(projects.core.domain)
    implementation(projects.intercom.domain)
    implementation(projects.iperf.domain)
    implementation(projects.connectivity.domain)
}