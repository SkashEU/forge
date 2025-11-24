plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.logger"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
        }
    }
}