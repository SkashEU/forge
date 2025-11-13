plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
}

subprojects {
    plugins.withId("maven-publish") {
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications.withType<MavenPublication>().all {
                    val basePath = project.path.removePrefix(":").replace(':', '-')

                    groupId = "com.skash.forge"
                    version = rootProject.version.toString()

                    artifactId =
                        when (name) {
                            "kotlinMultiplatform" -> basePath
                            else -> "$basePath-$name".lowercase()
                        }
                }
            }
        }

        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/SkashEU/forge")
                    credentials {
                        username = project.findProperty("gpr.user") as? String
                        password = project.findProperty("gpr.key") as? String
                    }
                }
            }
        }
    }
}
