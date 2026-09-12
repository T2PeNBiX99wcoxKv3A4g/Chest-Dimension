package io.github.ykysnk.chestdimension.level.levelgen.feature

import com.mojang.serialization.Codec
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.floor
import kotlin.math.sin

// Credits: ChatGPT
class TorchPathFeature(codec: Codec<TorchPathConfiguration>) : Feature<TorchPathConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<TorchPathConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val config = context.config()

        val seed = level.seed

        /*
         * Generate the path parameters from the world seed.
         *
         * Every chunk uses exactly the same values, so the path is global
         * and continuous across chunk boundaries.
         */
        val phase = hashToDouble(seed xor PHASE_1_SALT) * Math.PI * 2.0
        val phase2 = hashToDouble(seed xor PHASE_2_SALT) * Math.PI * 2.0
        val amplitude1 = 20.0 + hashToDouble(seed xor AMPLITUDE_1_SALT) * 40.0
        val amplitude2 = 8.0 + hashToDouble(seed xor AMPLITUDE_2_SALT) * 20.0
        val wavelength1 = 300.0 + hashToDouble(seed xor WAVELENGTH_1_SALT) * 300.0
        val wavelength2 = 100.0 + hashToDouble(seed xor WAVELENGTH_2_SALT) * 150.0

        /*
         * The whole path either runs along X or along Z.
         */
        val alongX = (seed and 1L) == 0L

        val minX = origin.x
        val maxX = origin.x + 15

        val minZ = origin.z
        val maxZ = origin.z + 15

        /*
         * minSpacing / maxSpacing describe the number of EMPTY BLOCKS
         * between torches.
         *
         * Therefore:
         *
         *     minSpacing = 1
         *         -> 🔥 . 🔥
         *
         *     maxSpacing = 4
         *         -> 🔥 . . . . 🔥
         *
         * The actual coordinate distance is therefore:
         *
         *     minSpacing + 1 .. maxSpacing + 1
         */
        val minGap = (config.minSpacing + 1).coerceAtLeast(1)
        val maxGap = (config.maxSpacing + 1).coerceAtLeast(minGap)
        var placed = false

        /*
         * A global block is larger than the maximum possible gap.
         *
         * Each block starts with a torch and is randomly partitioned
         * into valid gaps.
         *
         * Because the block ends exactly on a torch position, the
         * spacing across block boundaries is valid as well.
         */
        val blockLength = maxGap * BLOCK_MULTIPLIER

        if (alongX) {
            val minBlock = Math.floorDiv(minX, blockLength)
            val maxBlock = Math.floorDiv(maxX, blockLength)

            for (blockIndex in minBlock..maxBlock) {
                val torchCoordinates = generateTorchCoordinates(blockIndex, seed, minGap, maxGap, blockLength)

                for (x in torchCoordinates) {
                    /*
                     * Only modify the current chunk.
                     */
                    if (x !in minX..maxX) continue
                    val z = floor(
                        pathZ(
                            config,
                            x.toDouble(),
                            seed,
                            phase,
                            phase2,
                            amplitude1,
                            amplitude2,
                            wavelength1,
                            wavelength2
                        )
                    ).toInt()

                    /*
                     * The path itself is outside this chunk.
                     */
                    if (z !in minZ..maxZ) continue
                    if (placeTorch(level, x, z, origin.y)) placed = true
                }
            }
        } else {
            val minBlock = Math.floorDiv(minZ, blockLength)
            val maxBlock = Math.floorDiv(maxZ, blockLength)

            for (blockIndex in minBlock..maxBlock) {
                val torchCoordinates = generateTorchCoordinates(blockIndex, seed, minGap, maxGap, blockLength)

                for (z in torchCoordinates) {
                    /*
                     * Only modify the current chunk.
                     */
                    if (z !in minZ..maxZ) continue

                    val x = floor(
                        pathX(
                            config,
                            z.toDouble(),
                            seed,
                            phase,
                            phase2,
                            amplitude1,
                            amplitude2,
                            wavelength1,
                            wavelength2
                        )
                    ).toInt()

                    /*
                     * The path itself is outside this chunk.
                     */
                    if (x !in minX..maxX) continue
                    if (placeTorch(level, x, z, origin.y)) placed = true
                }
            }
        }

        return placed
    }

    /**
     * Generates all torch positions inside one global block.
     *
     * Example:
     *
     *     minGap = 2
     *     maxGap = 5
     *
     * A possible result is:
     *
     *     🔥 -- 🔥 ---- 🔥 - 🔥 ----- 🔥
     *       3      5      2       5
     *
     * Every gap is guaranteed to be between minGap and maxGap.
     *
     * The block always starts at a torch position.
     * The end of the block is also a torch position, but that final
     * position is omitted from the returned list because it is the
     * starting torch of the next block.
     */
    private fun generateTorchCoordinates(
        blockIndex: Int,
        seed: Long,
        minGap: Int,
        maxGap: Int,
        blockLength: Int
    ): List<Int> {
        val blockStart = blockIndex * blockLength

        /*
         * The total distance that needs to be partitioned.
         */
        var remainingDistance = blockLength

        /*
         * Determine how many gaps this block will contain.
         *
         * For N gaps:
         *
         *     N * minGap <= blockLength <= N * maxGap
         */
        val minimumGapCount = (blockLength + maxGap - 1) / maxGap
        val maximumGapCount = blockLength / minGap
        val randomForCount = hashToDouble(seed xor (blockIndex.toLong() * GAP_COUNT_SALT))
        val gapCount = minimumGapCount + (randomForCount * (maximumGapCount - minimumGapCount + 1)).toInt()
        val coordinates = ArrayList<Int>(gapCount)

        /*
         * The first torch is always at the block start.
         */
        coordinates += blockStart

        var currentOffset = 0

        for (gapIndex in 0 until gapCount) {
            val remainingGaps = gapCount - gapIndex - 1

            /*
             * The next gap must be large enough to satisfy minGap,
             * but small enough to leave enough distance for the
             * remaining gaps.
             */
            val minimumAllowedGap = maxOf(minGap, remainingDistance - remainingGaps * maxGap)
            val maximumAllowedGap = minOf(maxGap, remainingDistance - remainingGaps * minGap)
            val random =
                hashToDouble(seed xor (blockIndex.toLong() * BLOCK_RANDOM_SALT) xor (gapIndex.toLong() * GAP_RANDOM_SALT))
            val gap =
                if (minimumAllowedGap == maximumAllowedGap) minimumAllowedGap else minimumAllowedGap + (random * (maximumAllowedGap - minimumAllowedGap + 1)).toInt()

            currentOffset += gap
            remainingDistance -= gap

            /*
             * The final position is the start of the next block,
             * so don't add it here.
             */
            if (gapIndex < gapCount - 1) coordinates += blockStart + currentOffset
        }

        return coordinates
    }

    /**
     * Global path:
     *
     *     z = f(x)
     */
    private fun pathZ(
        config: TorchPathConfiguration,
        x: Double,
        seed: Long,
        phase: Double,
        phase2: Double,
        amplitude1: Double,
        amplitude2: Double,
        wavelength1: Double,
        wavelength2: Double
    ): Double {
        val offset = hashToDouble(seed xor OFFSET_X_SALT) * config.offset - config.offset / 2.0
        return offset + sin(x / wavelength1 * Math.PI * 2.0 + phase) * amplitude1 + sin(x / wavelength2 * Math.PI * 2.0 + phase2) * amplitude2
    }

    /**
     * Global path:
     *
     *     x = f(z)
     */
    private fun pathX(
        config: TorchPathConfiguration,
        z: Double,
        seed: Long,
        phase: Double,
        phase2: Double,
        amplitude1: Double,
        amplitude2: Double,
        wavelength1: Double,
        wavelength2: Double
    ): Double {
        val offset = hashToDouble(seed xor OFFSET_Z_SALT) * config.offset - config.offset / 2.0
        return offset + sin(z / wavelength1 * Math.PI * 2.0 + phase) * amplitude1 + sin(z / wavelength2 * Math.PI * 2.0 + phase2) * amplitude2
    }

    /**
     * Places a torch on the surface.
     *
     * This only modifies the current generation area.
     */
    private fun placeTorch(level: WorldGenLevel, x: Int, z: Int, y: Int): Boolean {
        var y = y
        while (y > level.minBuildHeight && level.getBlockState(BlockPos(x, y - 1, z)).isAir) y--
        val torchPos = BlockPos(x, y, z)
        if (!level.getBlockState(torchPos).isAir) return false
        val torchState = Blocks.TORCH.defaultBlockState()
        if (!torchState.canSurvive(level, torchPos)) return false
        level.setBlock(torchPos, torchState, 2)
        return true
    }

    /**
     * Deterministic 64-bit hash.
     *
     * Returns a value in [0, 1].
     */
    private fun hashToDouble(value: Long): Double {
        var x = value

        x = (x xor (x ushr 30)) * -4658895280553007687L
        x = (x xor (x ushr 27)) * -7723592293110705685L
        x = x xor (x ushr 31)

        return x.toULong().toDouble() / ULong.MAX_VALUE.toDouble()
    }

    companion object {
        private const val PHASE_1_SALT = 0x1234ABCDL
        private const val PHASE_2_SALT = 0x5678EF01L
        private const val AMPLITUDE_1_SALT = 0x11111111L
        private const val AMPLITUDE_2_SALT = 0x22222222L
        private const val WAVELENGTH_1_SALT = 0x33333333L
        private const val WAVELENGTH_2_SALT = 0x44444444L
        private const val OFFSET_X_SALT = 0x99999999L
        private const val OFFSET_Z_SALT = 0xAAAAAAAAL
        private const val GAP_COUNT_SALT = 0x13579BDF2468ACE0L
        private const val BLOCK_RANDOM_SALT = 0x2468ACE013579BDFL
        private const val GAP_RANDOM_SALT = 0x5F3759DFCAFEBABEL

        /*
         * Larger values provide more possible random compositions.
         *
         * 4 is enough for the normal spacing range and keeps generation
         * cheap.
         */
        private const val BLOCK_MULTIPLIER = 4
    }
}