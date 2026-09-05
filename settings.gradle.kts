@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "linphone.org maven repository"
            url = uri("https://download.linphone.org/maven_repository")
            content {
                includeGroup("org.linphone")
            }
        }
    }
}

rootProject.name = "Vale VoIP"
include(":app")
include(":feature:dialer")
include(":feature:call")
include(":feature:splash")
include(":feature:login")
include(":feature:home")
include(":feature:history")
include(":core:domain")
include(":core:data")
include(":core:telecom")
include(":core:sip")
include(":core:designsystem")
