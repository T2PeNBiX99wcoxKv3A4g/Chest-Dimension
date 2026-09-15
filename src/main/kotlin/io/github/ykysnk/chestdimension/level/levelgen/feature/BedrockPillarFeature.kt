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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
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

        if (random.nextInt(100) >= config.placeProbability) return false

        var placed = false
        val usedPositions = mutableSetOf<Long>()
        var x: Int
        var z: Int

        do {
            x = minX + random.nextInt(16)
            z = minZ + random.nextInt(16)
        } while (!usedPositions.add(BlockPos.asLong(x, 0, z)))

        if (placePillar(config, level, x, z, origin.y, random)) placed = true

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
        var startY = y
        while (startY > level.minBuildHeight && (level.getBlockState(
                BlockPos(
                    x,
                    startY - 1,
                    z
                )
            ).isAir || level.getBlockState(
                BlockPos(x, startY - 1, z)
            ).`is`(MCBlocks.WATER))
        ) startY--
        val faceY = startY
        for (i in 0 until 2) {
            val testY = startY - 1
            if (testY <= level.minBuildHeight) break
            if (level.getBlockState(BlockPos(x, testY, z)).isAir) break
            startY = testY
        }
        val bedrock = MCBlocks.BEDROCK.defaultBlockState()
        var placed = false
        val maxHeight = config.maxY.coerceAtMost(level.maxBuildHeight)
        val minHeight = (faceY + 10).coerceAtMost(maxHeight)
        if (minHeight > maxHeight) return false
        val randomHeight = random.nextIntBetweenInclusive(minHeight, maxHeight)

        val totalHeight = (randomHeight - startY).toDouble()
        if (totalHeight <= 0) return false

        val angle = random.nextDouble() * (Math.PI * 2.0)
        val dirX = cos(angle)
        val dirZ = sin(angle)
        val maxOffset = totalHeight * (random.nextDouble() * 0.08 + 0.08)

        var lastX: Int? = null
        var lastZ: Int? = null

        for (currY in startY until randomHeight) {
            val progress = (currY - startY) / totalHeight
            val currentOffset = maxOffset * progress * progress
            val blockX = (x + dirX * currentOffset).roundToInt()
            val blockZ = (z + dirZ * currentOffset).roundToInt()

            if (lastX != null && lastZ != null && (blockX != lastX || blockZ != lastZ)) {
                level.setBlock(BlockPos(blockX, currY - 1, lastZ), bedrock, 2)
                level.setBlock(BlockPos(blockX, currY - 1, blockZ), bedrock, 2)
            }

            val pos = BlockPos(blockX, currY, blockZ)
            level.setBlock(pos, bedrock, 2)
            placed = true

            lastX = blockX
            lastZ = blockZ
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