import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.agp)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

val properties = Properties().apply {
    load(FileInputStream(project.rootProject.file("electro.properties")))
}

android {
    namespace = "cn.tabidachi.electro"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.tabidachi.electro"
        minSdk = 28
        targetSdk = 34
        versionCode = 5
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        renderscriptTargetApi = 23
        renderscriptSupportModeEnabled = true
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(properties.getProperty("signing.storeFile"))
            storePassword = properties.getProperty("signing.storePassword")
            keyAlias = properties.getProperty("signing.keyAlias")
            keyPassword = properties.getProperty("signing.keyPassword")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        release {
            buildConfigField("String", "APP_CENTER_SECRET", properties.getProperty("appCenter.secret"))
            buildConfigField("String", "ELECTRO_SERVER_HOST", properties.getProperty("electro.server.host.release"))
            buildConfigField("String", "MINIO_URL", properties.getProperty("minio.url.release"))
            buildConfigField("String", "MINIO_ACCESS_KEY", properties.getProperty("minio.accessKey.release"))
            buildConfigField("String", "MINIO_SECRET_KEY", properties.getProperty("minio.secretKey.release"))
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            buildConfigField("String", "APP_CENTER_SECRET", properties.getProperty("appCenter.secret"))
            buildConfigField("String", "ELECTRO_SERVER_HOST", properties.getProperty("electro.server.host.debug"))
            buildConfigField("String", "MINIO_URL", properties.getProperty("minio.url.debug"))
            buildConfigField("String", "MINIO_ACCESS_KEY", properties.getProperty("minio.accessKey.debug"))
            buildConfigField("String", "MINIO_SECRET_KEY", properties.getProperty("minio.secretKey.debug"))
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += "distribute"
    productFlavors {
        create("appCenter") {
            dimension = "distribute"
        }
    }
    sourceSets {
        named("main") {
            jniLibs.srcDirs("libs")
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.accompanist)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.bundles.navigation.compose)
    implementation(libs.bundles.ktor)
    implementation(platform(libs.coil.bom))
    implementation(libs.bundles.coil)
    implementation(libs.bundles.room)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.compose.constraintlayout)
    implementation(libs.minio)
    implementation(libs.google.webrtc)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.appcenter)
    implementation(libs.tabler.icons)
    implementation(files("libs/AMap3DMap_9.6.0_AMapSearch_9.5.0_AMapLocation_6.2.0_20230116.jar"))
    implementation(libs.okhttp)
    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

}

kapt {
    correctErrorTypes = true
}
