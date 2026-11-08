// Same Kotlin and Compose versions as the ZapWell app, so the spike answers whether that code base can go to iOS.
plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("org.jetbrains.compose") version "1.12.2" apply false
}
