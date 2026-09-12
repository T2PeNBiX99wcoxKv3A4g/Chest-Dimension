package io.github.ykysnk.chestdimension.level.biome

import io.github.ykysnk.chestdimension.level.levelgen.feature.PlacedFeatures
import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.GenerationStep

object Biomes {
    val CHEST_PLATFORM_BIOME: ResourceKey<Biome> =
        ResourceKey.create(Registries.BIOME, NameSpaces.MOD("chest_platform_biome"))
    val CHEST_UNDEFINED_BIOME: ResourceKey<Biome> =
        ResourceKey.create(Registries.BIOME, NameSpaces.MOD("chest_undefined_biome"))

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
            .fogColor(0)
            .skyColor(0)
            .waterColor(0)
            .waterFogColor(0)
            .build()
    }

    fun createChestUndefinedBiome(context: BootstapContext<Biome>) {
        val placedFeatures = context.lookup(Registries.PLACED_FEATURE)
        val carvers = context.lookup(Registries.CONFIGURED_CARVER)
        val generationSettings = BiomeGenerationSettings.Builder(placedFeatures, carvers)
            .addFeature(
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                placedFeatures.getOrThrow(PlacedFeatures.TORCH_PATH)
            ).addFeature(
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                placedFeatures.getOrThrow(PlacedFeatures.BEDROCK_PILLAR)
            ).addFeature(
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                placedFeatures.getOrThrow(PlacedFeatures.FREEZE_TOP_LAYER)
            ).build()

        val biome = Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(-1f)
            .downfall(1f)
            .specialEffects(ChestUndefinedBiomeSpecialEffects)
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .generationSettings(generationSettings)
            .temperatureAdjustment(Biome.TemperatureModifier.NONE)
            .build()

        context.register(CHEST_UNDEFINED_BIOME, biome)
    }
}