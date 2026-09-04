package io.github.ykysnk.chestdimension.level.levelgen.feature

import com.mojang.serialization.Codec
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.BedrockPillarConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.max

// Credits: ChatGPT
class BedrockPillarFeature(codec: Codec<BedrockPillarConfiguration>) : Feature<BedrockPillarConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<BedrockPillarConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val config = context.config()
        val random = context.random()

        val chunkPos = ChunkPos(origin)

        val minX = chunkPos.minBlockX
        val minZ = chunkPos.minBlockZ

        val maxPillars = max(config.minPillars, config.maxPillars)
        val pillarCount = config.minPillars + random.nextInt(maxPillars - config.minPillars + 1)
        var placed = false
        val usedPositions = mutableSetOf<Long>()

        repeat(pillarCount) {
            var x: Int
            var z: Int

            do {
                x = minX + random.nextInt(16)
                z = minZ + random.nextInt(16)
            } while (!usedPositions.add(BlockPos.asLong(x, 0, z)))

            if (placePillar(config, level, x, z, origin.y, random)) placed = true
        }

        return placed
    }

    private fun placePillar(
        config: BedrockPillarConfiguration,
        level: WorldGenLevel,
        x: Int,
        z: Int,
        originY: Int,
        random: RandomSource
    ): Boolean {
        var y = originY
        while (y > level.minBuildHeight && level.getBlockState(BlockPos(x, y - 1, z)).isAir) y--
        val bedrock = Blocks.BEDROCK.defaultBlockState()
        var placed = false
        val minHeight = config.minHeight
        val maxHeight = config.maxHeight.coerceAtMost(level.maxBuildHeight)
        val randomHeight = random.nextIntBetweenInclusive(minHeight, maxHeight)

        for (y in y until randomHeight) {
            val pos = BlockPos(x, y, z)
            level.setBlock(pos, bedrock, 2)
            placed = true
        }

        return placed
    }
}