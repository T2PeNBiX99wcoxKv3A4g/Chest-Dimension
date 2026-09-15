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
}

// Should match your modid
rootProject.name = "chest-dimension"
