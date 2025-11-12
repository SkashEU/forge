# Forge

My personal collection of shareable & reusable modules for Kotlin & Compose Multiplatform.

It's a set of pre-built libraries that I add as dependencies in new projects.

When I start a new KMP app, I import these modules to skip the setup for common architectural components like state aware ViewModels, networking, and navigation.

## Supported Platforms

All modules are built with Kotlin Multiplatform, targeting:
*  **Android**
*  **iOS**
*  **JVM (Desktop)**
*  **Web**

## Available Modules

TODO


## How to Use (Installation)

To add Forge modules to your own KMP project from **GitHub Packages**:

1.  **Add the GitHub Packages Repository**
    Add the following to your `settings.gradle.kts`.

    ```kotlin
    // settings.gradle.kts
    dependencyResolutionManagement {
        repositories {
            mavenCentral()
            // Add GitHub Packages repository
            maven {
                url = uri("https://maven.pkg.github.com/SkashEU/forge")
            }
        }
    }
    ```

2.  **Add Module Dependencies**
    In your `shared/build.gradle.kts` file, add the modules you need.

    ```kotlin
    // shared/build.gradle.kts
    sourceSets {
        commonMain.dependencies {
        }
    }
    ```