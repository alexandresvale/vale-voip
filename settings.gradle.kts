pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
