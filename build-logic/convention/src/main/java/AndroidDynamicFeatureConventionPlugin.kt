import com.android.build.api.dsl.DynamicFeatureExtension
import com.specure.convention.ExtensionType
import com.specure.convention.addUiLayerDependencies
import com.specure.convention.configureAndroidCompose
import com.specure.convention.configureBuildTypes
import com.specure.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidDynamicFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("com.android.dynamic-feature")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<DynamicFeatureExtension> {
                configureKotlinAndroid(this)

                configureAndroidCompose(this)

                configureBuildTypes(
                    commonExtension = this,
                    extensionType = ExtensionType.DYNAMIC_FEATURE
                )
            }

            dependencies {
                addUiLayerDependencies(target)
                "testImplementation"(kotlin("test"))
            }
        }
    }
}