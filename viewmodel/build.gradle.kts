plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.viewmodel"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(projects.outcome)
            api(projects.navigation)
            api(projects.logger)
            api(projects.event)
        }
    }
}