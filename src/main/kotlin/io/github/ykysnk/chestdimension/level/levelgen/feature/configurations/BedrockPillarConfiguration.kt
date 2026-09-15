package io.github.ykysnk.chestdimension.level.levelgen.feature.configurations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

data class BedrockPillarConfiguration(
    val placeProbability: Int = 20,
    val maxY: Int = 150
) : FeatureConfiguration {
    companion object {
        val CODEC: Codec<BedrockPillarConfiguration> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(1, 100).fieldOf("place_probability")
                    .forGetter(BedrockPillarConfiguration::placeProbability),
                Codec.intRange(1, 200).fieldOf("max_y").forGetter(BedrockPillarConfiguration::maxY)
            ).apply(instance, ::BedrockPillarConfiguration)
        }
    }
}