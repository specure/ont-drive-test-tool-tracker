package com.cadrikmdev.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension<*,*,*,*,*,*>,
    extensionType: ExtensionType
) {
    commonExtension.run {

        buildFeatures {
            buildConfig = true
        }

        val hostname = gradleLocalProperties(rootDir, providers).getProperty("HOSTNAME")
        val speedTestFeatureEnabled =
            gradleLocalProperties(rootDir, providers).getProperty("FEATURE_SPEED_TEST_ENABLED")
                .toBoolean()
        when(extensionType) {
            ExtensionType.APPLICATION -> {
                extensions.configure<ApplicationExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(hostname, speedTestFeatureEnabled)
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled
                            )
                        }
                    }
                }
            }
            ExtensionType.LIBRARY -> {
                extensions.configure<LibraryExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(hostname, speedTestFeatureEnabled)
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled
                            )
                        }
                    }
                }
            }

            ExtensionType.DYNAMIC_FEATURE -> {
                extensions.configure<DynamicFeatureExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(hostname, speedTestFeatureEnabled)
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled
                            )
                            isMinifyEnabled = false
                        }
                    }
                }
            }
        }
    }
}

private fun BuildType.configureDebugBuildType(hostname: String, speedTestFeatureEnabled: Boolean) {
    buildConfigField("String", "BASE_URL", "\"$hostname\"")
    buildConfigField("boolean", "FEATURE_SPEED_TEST_ENABLED", "$speedTestFeatureEnabled")
}

private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    hostname: String,
    speedTestFeatureEnabled: Boolean,
) {
    buildConfigField("String", "BASE_URL", "\"$hostname\"")
    buildConfigField("boolean", "FEATURE_SPEED_TEST_ENABLED", "$speedTestFeatureEnabled")
    isMinifyEnabled = true
    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}