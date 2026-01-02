@file:Suppress("UnstableApiUsage")
pluginManagement {
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
        maven { url = uri("https://jitpack.io")  }
        maven {
            name = "linphone.org maven repository"
            url = uri("https://linphone.org/maven_repository/")
            content {
                includeGroup("org.linphone")
            }
        }
    }
}

rootProject.name = "Vale VoIP"
include(":app")
include(":telecom")
include(":pjsua2")
include(":domain")
include(":data")
