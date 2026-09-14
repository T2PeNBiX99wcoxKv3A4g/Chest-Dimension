pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
        maven("https://t2penbix99wcoxkv3a4g.github.io/Translation-Generator/") {
            name = "TranslationGenerator"
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
        kotlin("jvm") version providers.gradleProperty("jvm_version")
        kotlin("plugin.serialization") version providers.gradleProperty("jvm_version")
        id("io.github.ykysnk.translation-generator") version providers.gradleProperty("translation_generator_version")
    }
}

// Should match your modid
rootProject.name = "chest-dimension"
