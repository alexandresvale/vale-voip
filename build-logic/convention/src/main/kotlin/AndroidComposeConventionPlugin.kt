import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))

                add("implementation", libs.findLibrary("ui").get())
                add("implementation", libs.findLibrary("ui-graphics").get())
                add("implementation", libs.findLibrary("ui-tooling-preview").get())
                add("implementation", libs.findLibrary("material3").get())
                add("debugImplementation", libs.findLibrary("ui-tooling").get())
                add("debugImplementation", libs.findLibrary("ui-test-manifest").get())
            }
        }
    }
}