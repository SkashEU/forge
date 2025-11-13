import com.android.build.api.dsl.androidLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class InternalKMPModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            applyPluginsByName(
                "kotlinMultiplatform",
                "androidKotlinMultiplatformLibrary"
            )

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(JVM_TOOLCHAIN_VERSION)

                androidLibrary {
                    compileSdk = COMPILE_SDK
                    minSdk = MIN_SDK
                }

                jvm()

                js {
                    browser()
                    binaries.executable()
                }

                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                    binaries.executable()
                }

                iosX64 {
                    binaries.framework {
                        baseName = xcFrameworkName
                    }
                }
                iosArm64 {
                    binaries.framework {
                        baseName = xcFrameworkName
                    }
                }
                iosSimulatorArm64 {
                    binaries.framework {
                        baseName = xcFrameworkName
                    }
                }

                sourceSets.commonMain {
                    dependencies {
                        implementation(findLibraryByName("kotlin-stdlib"))
                        implementation(findLibraryByName("kotlinx-coroutines-core"))
                    }
                }

                sourceSets.jvmMain {
                    dependencies {
                        implementation(findLibraryByName("kotlinx-coroutinesSwing"))
                    }
                }

                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}
