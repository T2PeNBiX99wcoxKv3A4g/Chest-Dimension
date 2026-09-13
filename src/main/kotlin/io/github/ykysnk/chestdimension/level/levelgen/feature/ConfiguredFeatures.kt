package io.github.ykysnk.chestdimension.level.levelgen.feature

import io.github.ykysnk.chestdimension.NameSpaces
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.BedrockPillarConfiguration
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature

object ConfiguredFeatures {
    val TORCH_PATH: ResourceKey<ConfiguredFeature<*, *>> =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, NameSpaces.MOD("torch_path"))

    val BEDROCK_PILLAR: ResourceKey<ConfiguredFeature<*, *>> =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, NameSpaces.MOD("bedrock_pillar"))

    val TorchPathConfigured by lazy { ConfiguredFeature(Features.TORCH_PATH, TorchPathConfiguration()) }

    val BedrockPillarConfigured by lazy { ConfiguredFeature(Features.BEDROCK_PILLAR, BedrockPillarConfiguration()) }
}