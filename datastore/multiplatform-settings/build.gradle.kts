plugins {
    alias(libs.plugins.forge.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "com.skash.forge.datastore.multiplatformsettings"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.datastore.api)

            api(libs.multiplatform.settings.coroutines)
            api(libs.multiplatform.settings)
        }

        androidMain.dependencies {
            implementation(libs.androidx.datastore.datastore.preferences)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.russhwolf.multiplatform.settings.datastore)
        }

        iosMain.dependencies {
            implementation(libs.russhwolf.multiplatform.settings.datastore)
        }

        jvmMain.dependencies {
            implementation(libs.russhwolf.multiplatform.settings.datastore)
            implementation(libs.androidx.datastore.datastore.preferences)
        }

        webMain.dependencies {
            implementation(libs.multiplatform.settings.make.observable)
        }
    }
}