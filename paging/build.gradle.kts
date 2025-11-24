plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.paging"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.paging.common)
            api(projects.usecase)
        }
    }
}