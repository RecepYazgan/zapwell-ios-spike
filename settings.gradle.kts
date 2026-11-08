pluginManagement {
    repositories {
        maven("https://maven-central.storage-download.googleapis.com/maven2/") // Maven Central mirror without the GitHub redirect
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        maven("https://maven-central.storage-download.googleapis.com/maven2/") // Maven Central mirror without the GitHub redirect
        google()
        mavenCentral()
    }
}
rootProject.name = "ZapSpike"
include(":shared")
