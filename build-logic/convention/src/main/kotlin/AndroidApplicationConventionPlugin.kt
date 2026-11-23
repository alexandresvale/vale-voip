import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
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
