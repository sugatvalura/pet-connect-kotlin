/*
 * 🐾 Welcome to the Pet Connect Kotlin rewrite.
 *
 * This file is the bouncer at the door of the Gradle nightclub: it decides which
 * repositories are allowed in and which modules get to party. Keep it boring.
 * Boring settings files are happy settings files.
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS: no module is allowed to sneak in its own repository.
    // One source of truth for where jars come from. Chaos is for cats, not builds.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PetConnect"
include(":app")
