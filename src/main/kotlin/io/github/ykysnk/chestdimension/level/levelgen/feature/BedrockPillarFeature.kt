package io.github.ykysnk.chestdimension.level.levelgen.feature

import com.mojang.serialization.Codec
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.DeathBodyBlock
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.BedrockPillarConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.max
import net.minecraft.world.level.block.Blocks as MCBlocks

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

        if (random.nextInt(3) != 0) return false

        val maxPillars = max(config.minPillars, config.maxPillars)
        val pillarCount = random.nextIntBetweenInclusive(config.minPillars, maxPillars)
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
        y: Int,
        random: RandomSource
    ): Boolean {
        var y = y
        while (y > level.minBuildHeight && (level.getBlockState(BlockPos(x, y - 1, z)).isAir || level.getBlockState(
                BlockPos(x, y - 1, z)
            ).`is`(MCBlocks.WATER))
        ) y--
        val faceY = y
        for (i in 0 until 2) {
            val testY = y - 1
            if (testY <= level.minBuildHeight) break
            if (level.getBlockState(BlockPos(x, testY, z)).isAir) break
            y = testY
        }
        val bedrock = MCBlocks.BEDROCK.defaultBlockState()
        var placed = false
        val maxHeight = config.maxHeight.coerceAtMost(level.maxBuildHeight)
        val randomHeight = random.nextIntBetweenInclusive(faceY + 3, maxHeight)

        for (y in y until randomHeight) {
            val pos = BlockPos(x, y, z)
            level.setBlock(pos, bedrock, 2)
            placed = true
        }

        if (placed && random.nextInt(30) == 0) placeDeathBody(level, x, faceY, z, random)
        return placed
    }

    private fun placeDeathBody(level: WorldGenLevel, x: Int, y: Int, z: Int, random: RandomSource): Boolean {
        val direction = when (random.nextInt(4)) {
            0 -> Direction.NORTH
            1 -> Direction.SOUTH
            2 -> Direction.EAST
            else -> Direction.WEST
        }

        var bodyPos = BlockPos(x, y, z).relative(direction)

        var y = bodyPos.y
        while (y > level.minBuildHeight && (level.getBlockState(
                BlockPos(
                    bodyPos.x,
                    y - 1,
                    bodyPos.z
                )
            ).isAir || level.getBlockState(BlockPos(bodyPos.x, y - 1, bodyPos.z)).`is`(MCBlocks.WATER))
        ) y--

        bodyPos = BlockPos(bodyPos.x, y, bodyPos.z)

        if (!level.getBlockState(bodyPos).isAir && !level.getBlockState(bodyPos).`is`(MCBlocks.WATER)) return false
        val bodyState = Blocks.DEATH_BODY.defaultBlockState().setValue(DeathBodyBlock.FACING, direction)
        if (level.getBlockState(bodyPos).`is`(MCBlocks.WATER))
            bodyState.setValue(DeathBodyBlock.WATERLOGGED, true)
        if (!bodyState.canSurvive(level, bodyPos)) return false

        level.setBlock(bodyPos, bodyState, 2)
        if (random.nextInt(3) == 0) placeTorch(level, bodyPos.x, bodyPos.z, random)
        return true
    }

    private fun placeTorch(level: WorldGenLevel, x: Int, z: Int, random: RandomSource): Boolean {
        val offsetX = random.nextIntBetweenInclusive(-3, 3)
        val offsetZ = random.nextIntBetweenInclusive(-3, 3)
        var y = level.maxBuildHeight
        while (y > level.minBuildHeight && level.getBlockState(BlockPos(x + offsetX, y - 1, z + offsetZ)).isAir) y--

        val placePos = BlockPos(x + offsetX, y, z + offsetZ)
        if (!level.getBlockState(placePos).isAir) return false
        val torchState = MCBlocks.TORCH.defaultBlockState()
        if (!torchState.canSurvive(level, placePos)) return false

        level.setBlock(placePos, torchState, 2)
        return true
    }
}