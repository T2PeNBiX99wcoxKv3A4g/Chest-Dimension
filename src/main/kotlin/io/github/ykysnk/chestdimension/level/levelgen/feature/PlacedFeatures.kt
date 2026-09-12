package io.github.ykysnk.chestdimension.level.levelgen.feature

import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier

object PlacedFeatures {
    val TORCH_PATH: ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, NameSpaces.MOD("torch_path"))
    val BEDROCK_PILLAR: ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, NameSpaces.MOD("bedrock_pillar"))

    val FREEZE_TOP_LAYER: ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, NameSpaces.MINECRAFT("freeze_top_layer"))

    fun create(
        context: BootstapContext<PlacedFeature>,
        key: ResourceKey<PlacedFeature>,
        configuredFeatureKeys: ResourceKey<ConfiguredFeature<*, *>>,
        placement: List<PlacementModifier>
    ) {
        val configured = context.lookup(Registries.CONFIGURED_FEATURE)

        context.register(key, PlacedFeature(configured.getOrThrow(configuredFeatureKeys), placement))
    }
}