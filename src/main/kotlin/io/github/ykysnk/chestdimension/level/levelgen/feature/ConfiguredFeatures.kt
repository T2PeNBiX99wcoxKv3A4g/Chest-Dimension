package io.github.ykysnk.chestdimension.level.levelgen.feature

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature

object ConfiguredFeatures {
    val TORCH_PATH: ResourceKey<ConfiguredFeature<*, *>> =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id("torch_path"))

    val TorchPathConfigured by lazy { ConfiguredFeature(Features.TORCH_PATH, TorchPathConfiguration()) }
}