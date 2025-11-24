plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.usecase"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.outcome)
            api(projects.network.api.response)
        }
    }
}