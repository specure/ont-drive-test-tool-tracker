package com.specure.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
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
        val githubRepoApiUrl =
            gradleLocalProperties(rootDir, providers).getProperty("GITHUB_API_REPO_URL")
        val githubAccessToken =
            gradleLocalProperties(rootDir, providers).getProperty("GITHUB_API_TOKEN")
        when (extensionType) {
            ExtensionType.APPLICATION -> {
                extensions.configure<ApplicationExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                        }
                    }
                }
            }

            ExtensionType.LIBRARY -> {
                extensions.configure<LibraryExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                        }
                    }
                }
            }

            ExtensionType.DYNAMIC_FEATURE -> {
                extensions.configure<DynamicFeatureExtension> {
                    buildTypes {
                        debug {
                            configureDebugBuildType(
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                        }
                        release {
                            configureReleaseBuildType(
                                commonExtension,
                                hostname,
                                speedTestFeatureEnabled,
                                githubRepoApiUrl,
                                githubAccessToken
                            )
                            isMinifyEnabled = false
                        }
                    }
                }
            }
        }
    }
}

private fun BuildType.configureDebugBuildType(
    hostname: String,
    speedTestFeatureEnabled: Boolean,
    githubRepoApiUrl: String,
    githubAccessToken: String
) {
    buildConfigField("String", "BASE_URL", "\"$hostname\"")
    buildConfigField("boolean", "FEATURE_SPEED_TEST_ENABLED", "$speedTestFeatureEnabled")
    buildConfigField("String", "GITHUB_API_REPO_URL", "\"$githubRepoApiUrl\"")
    buildConfigField("String", "GITHUB_API_TOKEN", "\"$githubAccessToken\"")
}

private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    hostname: String,
    speedTestFeatureEnabled: Boolean,
    githubRepoApiUrl: String, githubAccessToken: String
) {
    buildConfigField("String", "BASE_URL", "\"$hostname\"")
    buildConfigField("boolean", "FEATURE_SPEED_TEST_ENABLED", "$speedTestFeatureEnabled")
    buildConfigField("String", "GITHUB_API_REPO_URL", "\"$githubRepoApiUrl\"")
    buildConfigField("String", "GITHUB_API_TOKEN", "\"$githubAccessToken\"")
    isMinifyEnabled = false
    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}