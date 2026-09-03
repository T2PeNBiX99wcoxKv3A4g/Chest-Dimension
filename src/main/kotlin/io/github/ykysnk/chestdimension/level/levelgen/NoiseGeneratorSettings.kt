package io.github.ykysnk.chestdimension.level.levelgen

import io.github.ykysnk.chestdimension.id
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.SurfaceRuleData
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.OverworldBiomeBuilder
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings
import net.minecraft.world.level.levelgen.NoiseRouterData

object NoiseGeneratorSettings {
    val UNDEFINED: ResourceKey<NoiseGeneratorSettings> = ResourceKey.create(Registries.NOISE_SETTINGS, id("undefined"))

    fun undefined(context: BootstapContext<*>, amplified: Boolean, large: Boolean): NoiseGeneratorSettings {
        return NoiseGeneratorSettings(
            NoiseSettings.UNDEFINED_NOISE_SETTINGS,
            Blocks.DIRT.defaultBlockState(),
            Blocks.AIR.defaultBlockState(),
            NoiseRouterData.overworld(
                context.lookup(Registries.DENSITY_FUNCTION),
                context.lookup(Registries.NOISE),
                large,
                amplified
            ),
            SurfaceRuleData.overworld(),
            OverworldBiomeBuilder().spawnTarget(),
            63,
            true,
            false,
            false,
            false
        )
    }
}