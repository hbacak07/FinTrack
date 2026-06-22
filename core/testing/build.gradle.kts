plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.hbacakk.fintrack.core.testing"
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

    // Test kütüphanelerini api ile dışa aç — bu modülü kullanan
    // her modül otomatik olarak bu test araçlarına sahip olur
    api(libs.junit5.api)
    api(libs.junit5.engine)
    api(libs.mockk.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
