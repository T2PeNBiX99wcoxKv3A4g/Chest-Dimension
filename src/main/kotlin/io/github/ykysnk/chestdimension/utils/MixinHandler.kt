package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.interfaces.PlayerInsideBlock
import net.minecraft.CrashReport
import net.minecraft.CrashReportCategory
import net.minecraft.ReportedException
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB

object MixinHandler {
    // Entity#checkInsideBlocks
    fun handleMovePlayer(player: ServerPlayer) {
        val aABB: AABB = player.boundingBox
        val blockPos = BlockPos.containing(aABB.minX + 1.0E-7, aABB.minY + 1.0E-7, aABB.minZ + 1.0E-7)
        val blockPos2 = BlockPos.containing(aABB.maxX - 1.0E-7, aABB.maxY - 1.0E-7, aABB.maxZ - 1.0E-7)
        if (player.serverLevel().hasChunksAt(blockPos, blockPos2)) {
            val mutableBlockPos = MutableBlockPos()
            var done = false

            for (x in blockPos.x..blockPos2.x) {
                for (y in blockPos.y..blockPos2.y) {
                    for (z in blockPos.z..blockPos2.z) {
                        mutableBlockPos.set(x, y, z)
                        val blockState: BlockState = player.serverLevel().getBlockState(mutableBlockPos)
                        val block = blockState.block

                        try {
                            if (block is PlayerInsideBlock && block.playerInside(
                                    blockState,
                                    player.serverLevel(),
                                    mutableBlockPos,
                                    player
                                )
                            )
                                done = true
                        } catch (throwable: Throwable) {
                            val crashReport = CrashReport.forThrowable(throwable, "Colliding entity with block")
                            val crashReportCategory = crashReport.addCategory("Block being collided with")
                            CrashReportCategory.populateBlockDetails(
                                crashReportCategory,
                                player.serverLevel(),
                                mutableBlockPos,
                                blockState
                            )
                            throw ReportedException(crashReport)
                        }
                        if (done) break
                    }
                    if (done) break
                }
                if (done) break
            }
        }
    }
}