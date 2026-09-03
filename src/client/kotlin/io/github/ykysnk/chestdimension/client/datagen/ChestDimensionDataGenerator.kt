package io.github.ykysnk.chestdimension.client.datagen

import io.github.ykysnk.chestdimension.client.datagen.provider.*
import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.level.levelgen.NoiseGeneratorSettings
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries

object ChestDimensionDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::DynamicRegistryProvider)
        pack.addProvider(::ModelProvider)
        pack.addProvider(::BlockTagProvider)
        pack.addProvider(::DamageTypeTagProvider)
        pack.addProvider(::BlockLootTableProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(Registries.BIOME) { context ->
            context.register(Biomes.CHEST_PLATFORM_BIOME, Biomes.ChestPlatformBiomeType)
            context.register(Biomes.CHEST_UNDEFINED_BIOME, Biomes.ChestUndefinedBiomeType)
        }
        registryBuilder.add(Registries.DIMENSION_TYPE) { context ->
            context.register(DimensionTypes.CHEST_PLATFORM, DimensionTypes.ChestPlatformDimensionType)
            context.register(DimensionTypes.CHEST_UNDEFINED, DimensionTypes.ChestUndefinedDimensionType)
        }
        registryBuilder.add(Registries.DAMAGE_TYPE) { context ->
            context.register(DamageTypes.EXPLOSION_BY_CHEST, DamageTypes.ExplosionByChestType)
            context.register(DamageTypes.EXPLOSION_BY_CHEST_INSIDE, DamageTypes.ExplosionByChestInsideType)
            context.register(DamageTypes.EXPLOSION_BY_TELEPORT_DOOR, DamageTypes.ExplosionByTeleportDoorType)
        }
        registryBuilder.add(Registries.NOISE_SETTINGS) { context ->
            context.register(
                NoiseGeneratorSettings.UNDEFINED, NoiseGeneratorSettings.undefined(
                    context,
                    amplified = false,
                    large = false
                )
            )
        }
    }
}