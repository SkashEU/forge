plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.multiplatformsettings"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.datastore.api)

            api("com.russhwolf:multiplatform-settings-coroutines:1.3.0")
            api("com.russhwolf:multiplatform-settings:1.3.0")
            api("com.russhwolf:multiplatform-settings-make-observable:1.3.0")
        }
    }
}