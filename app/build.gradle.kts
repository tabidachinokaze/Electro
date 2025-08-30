import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.compose)
}

val properties = Properties().apply {
    load(FileInputStream(project.rootProject.file("electro.properties")))
}

val commitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
}.standardOutput.asText.get().trim()

val commitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim()

val buildDateTime: String = SimpleDateFormat("yy.MMddHH").format(Date())

android {
    namespace = "cn.tabidachi.electro"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.tabidachi.electro"
        minSdk = 28
        targetSdk = 36
        versionCode = commitCount.toInt()

        versionName = "2.$buildDateTime-$commitHash"

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
            buildConfigField(
                "String",
                "APP_CENTER_SECRET",
                properties.getProperty("appCenter.secret")
            )
            buildConfigField(
                "String",
                "ELECTRO_SERVER_HOST",
                properties.getProperty("electro.server.host.release")
            )
            buildConfigField("String", "MINIO_URL", properties.getProperty("minio.url.release"))
            buildConfigField(
                "String",
                "MINIO_ACCESS_KEY",
                properties.getProperty("minio.accessKey.release")
            )
            buildConfigField(
                "String",
                "MINIO_SECRET_KEY",
                properties.getProperty("minio.secretKey.release")
            )
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            buildConfigField(
                "String",
                "APP_CENTER_SECRET",
                properties.getProperty("appCenter.secret")
            )
            buildConfigField(
                "String",
                "ELECTRO_SERVER_HOST",
                properties.getProperty("electro.server.host.debug")
            )
            buildConfigField("String", "MINIO_URL", properties.getProperty("minio.url.debug"))
            buildConfigField(
                "String",
                "MINIO_ACCESS_KEY",
                properties.getProperty("minio.accessKey.debug")
            )
            buildConfigField(
                "String",
                "MINIO_SECRET_KEY",
                properties.getProperty("minio.secretKey.debug")
            )
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

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.accompanist)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.navigation.compose)
    implementation(libs.bundles.ktor)
    implementation(platform(libs.coil.bom))
    implementation(libs.bundles.coil)
    implementation(libs.bundles.room)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.compose.constraintlayout)
    implementation(libs.minio)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.appcenter)
    implementation(libs.tabler.icons)
    implementation(files("libs/AMap3DMap_9.6.0_AMapSearch_9.5.0_AMapLocation_6.2.0_20230116.jar"))
    implementation(libs.okhttp)
    implementation(libs.webrtc.android)
    implementation(libs.kotlinx.serialization.json)
    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

}
