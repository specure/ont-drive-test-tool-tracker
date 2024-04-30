import com.android.build.api.dsl.LibraryExtension
import com.cadrikmdev.convention.ExtensionType
import com.cadrikmdev.convention.configureBuildTypes
import com.cadrikmdev.convention.configureKotlinAndroid
import com.cadrikmdev.convention.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class JvmLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureKotlinJvm()
        }
    }
}