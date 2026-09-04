package io.github.ykysnk.chestdimension.level.levelgen.feature

import com.mojang.serialization.Codec
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.sin

// Credits: ChatGPT
class TorchPathFeature(codec: Codec<TorchPathConfiguration>) : Feature<TorchPathConfiguration>(codec) {
    override fun place(
        context: FeaturePlaceContext<TorchPathConfiguration>
    ): Boolean {
        val level = context.level()
        val origin = context.origin()
        val config = context.config()

        val seed = level.seed

        val phase = hashToDouble(seed xor 0x1234ABCDL) * Math.PI * 2.0
        val phase2 = hashToDouble(seed xor 0x5678EF01L) * Math.PI * 2.0

        val amplitude1 = 20.0 + hashToDouble(seed xor 0x11111111L) * 40.0
        val amplitude2 = 8.0 + hashToDouble(seed xor 0x22222222L) * 20.0

        val wavelength1 = 300.0 + hashToDouble(seed xor 0x33333333L) * 300.0
        val wavelength2 = 100.0 + hashToDouble(seed xor 0x44444444L) * 150.0

        val alongX = (seed and 1L) == 0L

        val minX = origin.x
        val maxX = origin.x + 15
        val minZ = origin.z
        val maxZ = origin.z + 15

        val spacingPhase = seed xor 0xCAFEBABEL

        var placed = false

        if (alongX) {
            for (x in minX..maxX) {
                val point = pathZ(
                    x.toDouble(),
                    seed,
                    phase,
                    phase2,
                    amplitude1,
                    amplitude2,
                    wavelength1,
                    wavelength2
                )

                val z = point.toInt()

                if (z !in minZ..maxZ) continue

                if (!isTorchPosition(x, z, seed, spacingPhase, config)) continue
                if (placeTorch(level, x, z, origin.y)) {
                    placed = true
                }
            }
        } else {
            for (z in minZ..maxZ) {
                val point = pathX(
                    z.toDouble(),
                    seed,
                    phase,
                    phase2,
                    amplitude1,
                    amplitude2,
                    wavelength1,
                    wavelength2
                )

                val x = point.toInt()

                if (x !in minX..maxX) continue

                if (!isTorchPosition(z, x, seed, spacingPhase, config)) continue
                if (placeTorch(level, x, z, origin.y)) {
                    placed = true
                }
            }
        }

        return placed
    }

    private fun pathZ(
        x: Double,
        seed: Long,
        phase: Double,
        phase2: Double,
        amplitude1: Double,
        amplitude2: Double,
        wavelength1: Double,
        wavelength2: Double
    ): Double {
        val offset = hashToDouble(seed xor 0x99999999L) * 10000.0
        return offset + sin(x / wavelength1 * Math.PI * 2.0 + phase) * amplitude1 + sin(x / wavelength2 * Math.PI * 2.0 + phase2) * amplitude2
    }

    private fun pathX(
        z: Double,
        seed: Long,
        phase: Double,
        phase2: Double,
        amplitude1: Double,
        amplitude2: Double,
        wavelength1: Double,
        wavelength2: Double
    ): Double {
        val offset = hashToDouble(seed xor 0xAAAAAAAAL) * 10000.0
        return offset + sin(z / wavelength1 * Math.PI * 2.0 + phase) * amplitude1 + sin(z / wavelength2 * Math.PI * 2.0 + phase2) * amplitude2
    }

    private fun isTorchPosition(
        mainCoordinate: Int,
        secondaryCoordinate: Int,
        seed: Long,
        spacingPhase: Long,
        config: TorchPathConfiguration
    ): Boolean {
        val index = floorDiv(mainCoordinate, config.maxSpacing)
        val random =
            hashToDouble(seed + spacingPhase + index * -7046029254386353131L + secondaryCoordinate * -4417276706812531889L)
        val interval = config.minSpacing + (random * (config.maxSpacing - config.minSpacing + 1)).toInt()
        return floorMod(mainCoordinate, config.maxSpacing) < interval && floorMod(
            mainCoordinate,
            config.maxSpacing
        ) == 0
    }

    private fun placeTorch(
        level: WorldGenLevel,
        x: Int,
        z: Int,
        originY: Int
    ): Boolean {
        var y = originY
        while (y > level.minBuildHeight && level.getBlockState(BlockPos(x, y - 1, z)).isAir) y--
        val torchPos = BlockPos(x, y, z)
        if (!level.getBlockState(torchPos).isAir) return false
        val torchState = Blocks.TORCH.defaultBlockState()
        if (!torchState.canSurvive(level, torchPos)) return false
        level.setBlock(torchPos, torchState, 2)
        return true
    }

    private fun hashToDouble(value: Long): Double {
        var x = value

        x = (x xor (x ushr 30)) * -4658895280553007687L
        x = (x xor (x ushr 27)) * -7723592293110705685L
        x = x xor (x ushr 31)

        return (x.toULong().toDouble() / ULong.MAX_VALUE.toDouble())
    }

    private fun floorMod(value: Int, divisor: Int): Int = Math.floorMod(value, divisor)

    private fun floorDiv(value: Int, divisor: Int): Int = Math.floorDiv(value, divisor)
}