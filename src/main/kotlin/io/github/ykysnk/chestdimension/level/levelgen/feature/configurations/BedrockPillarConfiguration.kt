package io.github.ykysnk.chestdimension.level.levelgen.feature.configurations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

data class BedrockPillarConfiguration(
    val minPillars: Int = 1,
    val maxPillars: Int = 3,
    val maxHeight: Int = 150
) : FeatureConfiguration {
    companion object {
        val CODEC: Codec<BedrockPillarConfiguration> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(1, 64).fieldOf("min_pillars").forGetter(BedrockPillarConfiguration::minPillars),
                Codec.intRange(1, 64).fieldOf("max_pillars").forGetter(BedrockPillarConfiguration::maxPillars),
                Codec.intRange(1, 200).fieldOf("max_height").forGetter(BedrockPillarConfiguration::maxHeight)
            ).apply(instance, ::BedrockPillarConfiguration)
        }
    }
}