pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

rootProject.name = "SignalTracker"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:presentation:designsystem")
include(":core:presentation:service")
include(":core:presentation:ui")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:connectivity")
include(":track:data")
include(":track:domain")
include(":track:location")
include(":track:network")
include(":track:presentation")
include(":iperf-jni-upload:iperf")
include(":iperf-jni-download:iperf")



include(":permissions:data")
include(":permissions:domain")
include(":permissions:presentation")
include(":iperf:data")
include(":iperf:domain")
include(":iperf:presentation")
