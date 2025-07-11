pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.jetbrains.kotlin.android") version "2.0.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // ✅ Needed for Kotlin 2.0 + Compose
        id("com.android.application") version "8.4.0" // Or your AGP version
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "carlockcontrol"
include(":app")
