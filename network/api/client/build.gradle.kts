plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.network.client"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.network.api.request)
            api(projects.network.api.response)
        }
    }
}