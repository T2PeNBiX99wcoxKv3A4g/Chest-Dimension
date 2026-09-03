package io.github.ykysnk.chestdimension.level.biome

import io.github.ykysnk.chestdimension.id
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.MobSpawnSettings

object Biomes {
    val CHEST_PLATFORM_BIOME: ResourceKey<Biome> = ResourceKey.create(Registries.BIOME, id("chest_platform_biome"))
    val CHEST_UNDEFINED_BIOME: ResourceKey<Biome> = ResourceKey.create(Registries.BIOME, id("chest_undefined_biome"))

    val ChestPlatformBiomeType: Biome by lazy {
        Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.5f)
            .downfall(0.5f)
            .specialEffects(ChestPlatformBiomeSpecialEffects)
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .generationSettings(BiomeGenerationSettings.EMPTY)
            .temperatureAdjustment(Biome.TemperatureModifier.NONE)
            .build()
    }

    val ChestUndefinedBiomeType: Biome by lazy {
        Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.5f)
            .downfall(0.5f)
            .specialEffects(ChestUndefinedBiomeSpecialEffects)
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .generationSettings(BiomeGenerationSettings.EMPTY)
            .temperatureAdjustment(Biome.TemperatureModifier.NONE)
            .build()
    }

    private val ChestPlatformBiomeSpecialEffects: BiomeSpecialEffects by lazy {
        BiomeSpecialEffects.Builder()
            .fogColor(12638463)
            .skyColor(8103167)
            .waterColor(4159204)
            .waterFogColor(329011)
            .build()
    }

    private val ChestUndefinedBiomeSpecialEffects: BiomeSpecialEffects by lazy {
        BiomeSpecialEffects.Builder()
            .fogColor(12638463)
            .skyColor(8103167)
            .waterColor(4159204)
            .waterFogColor(329011)
            .build()
    }
}