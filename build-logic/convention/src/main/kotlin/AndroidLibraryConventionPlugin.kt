import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            extensions.configure<LibraryAndroidComponentsExtension> {
                beforeVariants { variantBuilder ->
                    val hasAndroidTestSource = projectDir.resolve("src/androidTest")
                        .walkTopDown()
                        .any { it.isFile && (it.extension == "kt" || it.extension == "java") }
                    variantBuilder.androidTest.enable = hasAndroidTestSource
                }
            }
        }
    }
}
