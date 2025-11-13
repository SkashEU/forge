import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import kotlin.jvm.optionals.getOrNull

private const val FALLBACK_JVM_VERSION = 21
private const val FALLBACK_ANDROID_MIN_SDK = 20
private const val FALLBACK_ANDROID_COMPILE_SDK_VERSION = 36

val Project.versionCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.COMPILE_SDK
    get() =
        versionCatalog
            .findIntVersion("android-compileSdk")
            ?: FALLBACK_ANDROID_COMPILE_SDK_VERSION

val Project.MIN_SDK
    get() =
        versionCatalog
            .findIntVersion("android-minSdk")
            ?: FALLBACK_ANDROID_MIN_SDK

val Project.JVM_TOOLCHAIN_VERSION
    get() =
        versionCatalog
            .findIntVersion("jvm-toolchain")
            ?: FALLBACK_JVM_VERSION

val Project.composeExtension
    get() = project.extensions.create("composeInternal", ComposeExtension::class.java, project)

val Project.xcFrameworkName
    get() = project.name.split("-").joinToString("") {
        it.replaceFirstChar(Char::titlecase)
    } + "Kit"


fun Project.applyPluginsByName(vararg names: String) =
    names
        .map { versionCatalog.findPlugin(it) }
        .mapNotNull { it.getOrNull()?.orNull?.pluginId }
        .forEach { pluginManager.apply(it) }

fun VersionCatalog.findIntVersion(versionName: String): Int? =
    findVersion(versionName)
        .getOrNull()
        ?.requiredVersion
        ?.toIntOrNull()

fun Project.findLibraryByName(
    nameInVersionCatalog: String
): Provider<MinimalExternalModuleDependency> = project.versionCatalog.findLibrary(nameInVersionCatalog).get()
