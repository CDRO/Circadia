pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("build-logic")

rootProject.name = "Circadia"
include(":app")
include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:datastore")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":feature:persons")
include(":feature:timeline")
include(":feature:export")
include(":widget")
