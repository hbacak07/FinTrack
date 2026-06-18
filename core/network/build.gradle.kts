plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)

}

android {
    namespace = "com.hbacakk.fintrack.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":domain"))

    api(libs.okhttp.core)
    api(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)
}