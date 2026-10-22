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
        // Only for FreeDroidWarn, built from its public sources by JitPack.
        maven("https://jitpack.io") {
            content { includeGroup("com.github.woheller69") }
        }
    }
}

rootProject.name = "Bigly"
include(":app")
