rootProject.name = "forge"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":examples:composeApp")
include(":network")
include(":network:api")
include(":network:api:client")
include(":network:api:request")
include(":network:api:session")
include(":network:api:response")
include(":network:ktor")
include(":outcome")
include(":viewmodel")
include(":navigation")
include(":logger")
include(":event")
include(":usecase")
include(":paging")
include(":datastore")
include(":datastore:api")
include(":datastore:multiplatform-settings")
