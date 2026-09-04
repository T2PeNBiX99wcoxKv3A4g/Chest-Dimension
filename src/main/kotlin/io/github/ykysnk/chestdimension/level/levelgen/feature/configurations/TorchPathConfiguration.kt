package io.github.ykysnk.chestdimension.level.levelgen.feature.configurations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

data class TorchPathConfiguration(
    val length: Int = 256,
    val minSpacing: Int = 6,
    val maxSpacing: Int = 15,
    val minTriangle: Double = 0.0,
    val maxTriangle: Double = 0.15
) : FeatureConfiguration {
    companion object {
        val CODEC: Codec<TorchPathConfiguration> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("length").orElse(256).forGetter(TorchPathConfiguration::length),
                Codec.INT.fieldOf("min_spacing").orElse(6).forGetter(TorchPathConfiguration::minSpacing),
                Codec.INT.fieldOf("max_spacing").orElse(15).forGetter(TorchPathConfiguration::maxSpacing),
                Codec.DOUBLE.fieldOf("min_triangle").orElse(0.0).forGetter(TorchPathConfiguration::minTriangle),
                Codec.DOUBLE.fieldOf("max_triangle").orElse(0.15).forGetter(TorchPathConfiguration::maxTriangle)
            ).apply(it, ::TorchPathConfiguration)
        }
    }
}