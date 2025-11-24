plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.network.session"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.network.api.client)
        }
    }
}