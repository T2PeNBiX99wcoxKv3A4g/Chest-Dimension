package io.github.ykysnk.chestdimension.client.datagen

import io.github.ykysnk.chestdimension.client.datagen.provider.*
import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.level.levelgen.NoiseGeneratorSettings
import io.github.ykysnk.chestdimension.level.levelgen.feature.ConfiguredFeatures
import io.github.ykysnk.chestdimension.level.levelgen.feature.PlacedFeatures
import io.github.ykysnk.chestdimension.level.levelgen.structure.StructureSets
import io.github.ykysnk.chestdimension.level.levelgen.structure.Structures
import io.github.ykysnk.chestdimension.level.levelgen.structure.TemplatePools
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.CountPlacement
import net.minecraft.world.level.levelgen.placement.InSquarePlacement

object ChestDimensionDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::DynamicRegistryProvider)
        pack.addProvider(::ModelProvider)
        pack.addProvider(::BlockTagProvider)
        pack.addProvider(::ItemTagProvider)
        pack.addProvider(::DamageTypeTagProvider)
        pack.addProvider(::BlockLootTableProvider)
        pack.addProvider(::SimpleLootTableProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE) { context ->
            context.register(ConfiguredFeatures.TORCH_PATH, ConfiguredFeatures.TorchPathConfigured)
            context.register(ConfiguredFeatures.BEDROCK_PILLAR, ConfiguredFeatures.BedrockPillarConfigured)
        }
        registryBuilder.add(Registries.PLACED_FEATURE) { context ->
            PlacedFeatures.create(
                context, PlacedFeatures.TORCH_PATH, ConfiguredFeatures.TORCH_PATH, listOf(
                    CountPlacement.of(1),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                    BiomeFilter.biome()
                )
            )
            PlacedFeatures.create(
                context, PlacedFeatures.BEDROCK_PILLAR, ConfiguredFeatures.BEDROCK_PILLAR, listOf(
                    CountPlacement.of(1),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                    BiomeFilter.biome()
                )
            )
        }
        registryBuilder.add(Registries.BIOME) { context ->
            context.register(Biomes.PLATFORM, Biomes.PlatformType)
            Biomes.createGraveyardBiome(context)
        }
        registryBuilder.add(Registries.DIMENSION_TYPE) { context ->
            context.register(DimensionTypes.PLATFORM, DimensionTypes.PlatformDimensionType)
            context.register(DimensionTypes.UNDEFINED, DimensionTypes.UndefinedDimensionType)
        }
        registryBuilder.add(Registries.DAMAGE_TYPE) { context ->
            context.register(DamageTypes.EXPLOSION_BY_CHEST, DamageTypes.ExplosionByChestType)
            context.register(DamageTypes.EXPLOSION_BY_CHEST_INSIDE, DamageTypes.ExplosionByChestInsideType)
            context.register(DamageTypes.EXPLOSION_BY_TELEPORT_DOOR, DamageTypes.ExplosionByTeleportDoorType)
        }
        registryBuilder.add(Registries.NOISE_SETTINGS) { context ->
            context.register(NoiseGeneratorSettings.UNDEFINED, NoiseGeneratorSettings.undefined(context))
        }
        registryBuilder.add(Registries.TEMPLATE_POOL, TemplatePools::bootstrap)
        registryBuilder.add(Registries.STRUCTURE, Structures::bootstrap)
        registryBuilder.add(Registries.STRUCTURE_SET, StructureSets::bootstrap)
    }
}