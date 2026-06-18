import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.plugin.use.PluginDependency

plugins {
    `kotlin-dsl`
}

group = "com.hbacakk.fintrack.buildlogic"

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    compileOnly(libs.plugins.compose.compiler.toDep())
    compileOnly(libs.plugins.hilt.toDep())
    compileOnly(libs.plugins.ksp.toDep())
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "fintrack.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "fintrack.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "fintrack.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "fintrack.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "fintrack.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("hilt") {
            id = "fintrack.hilt"
            implementationClass = "HiltConventionPlugin"
        }
    }
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}