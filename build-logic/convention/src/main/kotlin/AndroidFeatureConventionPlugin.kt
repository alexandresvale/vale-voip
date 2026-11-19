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
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(mapOf("path" to ":core:domain")))
                add("implementation", project(mapOf("path" to ":core:designsystem")))
                add("implementation", project(mapOf("path" to ":core:navigation")))
                add("implementation", project(mapOf("path" to ":core:analytics")))
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("material-icons-extended").get())
                add("implementation", libs.findLibrary("material3").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
                add("implementation", libs.findLibrary("timber").get())

                // Dependências Base para Testes Unitários e de Integração
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("mockk").get())
            }
        }
    }
}
