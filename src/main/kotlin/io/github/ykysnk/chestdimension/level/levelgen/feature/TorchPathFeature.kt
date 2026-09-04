package io.github.ykysnk.chestdimension.level.levelgen.feature

import com.mojang.serialization.Codec
import io.github.ykysnk.chestdimension.level.levelgen.feature.configurations.TorchPathConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.cos
import kotlin.math.sin

class TorchPathFeature(codec: Codec<TorchPathConfiguration>) : Feature<TorchPathConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<TorchPathConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val random = context.random()
        val config = context.config()
        var x = origin.x.toDouble()
        var z = origin.z.toDouble()
        var angle = random.nextDouble() * Math.PI * 2
        var distance = 0

        while (distance <= config.length) {
            val posX = x.toInt()
            val posZ = z.toInt()
            var y = origin.y

            while (y > level.minBuildHeight && !level.getBlockState(BlockPos(posX, y, posZ)).isAir) {
                y--
            }

            val torchPos = BlockPos(posX, y, posZ)
            val torchState = Blocks.TORCH.defaultBlockState()

            if (torchState.canSurvive(level, torchPos)) level.setBlock(torchPos, torchState, 2)

            val step = random.nextInt(config.minSpacing, config.maxSpacing + 1)

            x += cos(angle) * step
            z += sin(angle) * step
            angle += random.triangle(config.minTriangle, config.maxTriangle)
            distance += step
        }

        return true
    }
}