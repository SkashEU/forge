plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.datastore"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
    }
}