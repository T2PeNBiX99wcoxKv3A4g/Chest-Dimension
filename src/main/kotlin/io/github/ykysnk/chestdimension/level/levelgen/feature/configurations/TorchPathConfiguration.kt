package io.github.ykysnk.chestdimension.level.levelgen.feature.configurations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

data class TorchPathConfiguration(
    val minSpacing: Int = 6,
    val maxSpacing: Int = 15
) : FeatureConfiguration {
    companion object {
        val CODEC: Codec<TorchPathConfiguration> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("min_spacing").orElse(6).forGetter(TorchPathConfiguration::minSpacing),
                Codec.INT.fieldOf("max_spacing").orElse(15).forGetter(TorchPathConfiguration::maxSpacing)
            ).apply(it, ::TorchPathConfiguration)
        }
    }
}