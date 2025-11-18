plugins {
    alias(libs.plugins.forge.compose.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.navigatiion.nav2"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.navigation.api)
            api(libs.androidx.navigation.compose)
        }
    }
}