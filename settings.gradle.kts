/**
 * How does this work -> https://developer.android.com/studio/preview/features#settings-gradle
 */
pluginManagement {
    /**
     * Repos for root build.gradle/buildscript/dependencies
     */
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven ("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    /**
     * Repos for all modules. Previously was in build.gradle/allprojects/repositories
     */
    repositories {

        google()
        mavenCentral()
        maven ("https://jitpack.io")
    }
}



include(":app")
