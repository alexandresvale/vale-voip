import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("valevoip.android.library")
                apply("valevoip.android.compose")
                apply("valevoip.android.hilt")
            }

            dependencies {
                add("implementation", project(mapOf("path" to ":core:domain")))
                add("implementation", project(mapOf("path" to ":core:designsystem")))
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("material-icons-extended").get())
                add("implementation", libs.findLibrary("material3").get())
            }
        }
    }
}