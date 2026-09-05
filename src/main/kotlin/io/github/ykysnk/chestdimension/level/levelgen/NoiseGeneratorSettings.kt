package io.github.ykysnk.chestdimension.level.levelgen

import io.github.ykysnk.chestdimension.id
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.OverworldBiomeBuilder
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings

object NoiseGeneratorSettings {
    val CHEST_UNDEFINED: ResourceKey<NoiseGeneratorSettings> =
        ResourceKey.create(Registries.NOISE_SETTINGS, id("chest_undefined"))

    fun undefined(context: BootstapContext<*>, amplified: Boolean, large: Boolean): NoiseGeneratorSettings {
        return NoiseGeneratorSettings(
            NoiseSettings.UNDEFINED_NOISE_SETTINGS,
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            NoiseRouterData.undefined(
                context.lookup(Registries.DENSITY_FUNCTION),
                context.lookup(Registries.NOISE),
                large,
                amplified
            ),
            SurfaceRuleData.undefined(true, false, true),
            OverworldBiomeBuilder().spawnTarget(),
            0,
            true,
            false,
            false,
            false
        )
    }
}