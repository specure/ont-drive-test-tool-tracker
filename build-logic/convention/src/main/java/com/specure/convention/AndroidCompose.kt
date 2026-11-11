package com.specure.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension
) {
    commonExtension.buildFeatures.apply { compose = true }

    commonExtension.composeOptions.apply {
        kotlinCompilerExtensionVersion = libs.findVersion("composeCompiler").get().toString()
    }

    this@configureAndroidCompose.dependencies {
        val bom = libs.findLibrary("androidx.compose.bom").get()
        "api"(platform(bom))
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))
        "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
    }
}
