import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }

android {
    namespace = "com.huyi.recap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.huyi.recap"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
        ndk { abiFilters += "arm64-v8a" }
    }

    ndkVersion = "27.1.12297006"
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
    buildFeatures { compose = true; buildConfig = true }
    // khóa nằm trong local.properties (đã .gitignore): DEEPSEEK_API_KEY=...
    defaultConfig {
        fun cfg(k: String, d: String = "") = "\"${localProps.getProperty(k, d)}\""
        buildConfigField("String", "DEEPSEEK_API_KEY", cfg("DEEPSEEK_API_KEY"))
    }

    // ký release: keystore ngoài repo (~/.recap-keys), mật khẩu trong local.properties (RELEASE_*); thiếu ⇒ bỏ qua, chỉ build debug được
    val relStore = localProps.getProperty("RELEASE_STORE_FILE")
    signingConfigs {
        if (relStore != null) create("release") {
            storeFile = file(relStore)
            storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.activity.compose)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.mlkit.ocr)
    implementation(libs.play.auth)
    implementation(libs.work)
    implementation(files("libs/sherpa-onnx-1.13.8.aar"))   // STT Zipformer-vi (k2-fsa sherpa-onnx, arm64)
}
