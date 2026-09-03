pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
        kotlin("jvm") version providers.gradleProperty("jvm_version")
        kotlin("plugin.serialization") version providers.gradleProperty("jvm_version")
    }
}

// Should match your modid
rootProject.name = "chest-dimension"
