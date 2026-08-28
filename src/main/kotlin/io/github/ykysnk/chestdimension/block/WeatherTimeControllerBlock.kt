package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.world.inventory.WeatherTimeControllerFactory
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class WeatherTimeControllerBlock(properties: Properties) : Block(properties) {
    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            (player as? ServerPlayer)?.let { serverPlayer ->
                (level as? ServerLevel)?.let {
                    serverPlayer.openMenu(WeatherTimeControllerFactory(it))
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}