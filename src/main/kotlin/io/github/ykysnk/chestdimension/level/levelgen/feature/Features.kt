@file:Suppress("MemberVisibilityCanBePrivate", "SameParameterValue", "unused")

package io.github.ykysnk.chestdimension.level.levelgen.feature

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

object Features : RegistryHelper<Feature<out FeatureConfiguration>>() {
    private fun <C : FeatureConfiguration, F : Feature<C>> register(
        key: String,
        value: F
    ): F = register(Registry.register(BuiltInRegistries.FEATURE, id(key), value))

    val TORCH_PATH = register("torch_path", TorchPathFeature(TorchPathConfiguration.CODEC))
}