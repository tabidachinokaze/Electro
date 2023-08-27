pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://dl.bintray.com/google/webrtc/")
        maven("https://jitpack.io")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    }
}
rootProject.name = "Electro"
include(":app")
