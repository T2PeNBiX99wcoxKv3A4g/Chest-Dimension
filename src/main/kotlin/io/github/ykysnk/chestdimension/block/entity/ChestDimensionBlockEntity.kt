package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.extensions.openedChestDimension
import io.github.ykysnk.chestdimension.extensions.openedChestPos
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ChestLidController
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.minecraft.world.level.block.entity.LidBlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class ChestDimensionBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.CHEST_DIMENSION, pos, blockState), LidBlockEntity {
    companion object {
        @Suppress("unused")
        fun lidAnimateTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: ChestDimensionBlockEntity) {
            blockEntity.chestLidController.tickLid()
        }
    }

    private val chestLidController = ChestLidController()
    private val openersCounter: ContainerOpenersCounter = object : ContainerOpenersCounter() {
        override fun onOpen(level: Level, pos: BlockPos, state: BlockState) {
            level.playSound(
                null,
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5,
                SoundEvents.ENDER_CHEST_OPEN,
                SoundSource.BLOCKS,
                0.5f,
                level.random.nextFloat() * 0.1f + 0.9f
            )
        }

        override fun onClose(level: Level, pos: BlockPos, state: BlockState) {
            level.playSound(
                null,
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5,
                SoundEvents.ENDER_CHEST_CLOSE,
                SoundSource.BLOCKS,
                0.5f,
                level.random.nextFloat() * 0.1f + 0.9f
            )
        }

        override fun openerCountChanged(level: Level, pos: BlockPos, state: BlockState, count: Int, openCount: Int) {
            level.blockEvent(worldPosition, Blocks.CHEST_DIMENSION, 1, openCount)
        }

        override fun isOwnContainer(player: Player): Boolean = playerCache.contains(player)
    }

    var uuid: UUID = UUIDManager.randomUUID()
        private set

    val playerCache: HashSet<Player> = HashSet()

    override fun triggerEvent(id: Int, type: Int): Boolean {
        if (id == 1) {
            chestLidController.shouldBeOpen(type > 0)
            return true
        } else {
            return super.triggerEvent(id, type)
        }
    }

    override fun setLevel(level: Level) {
        super.setLevel(level)
        (level as? ServerLevel)?.let {
            ChestLevelManager.setActive(uuid)
            UUIDManager.setChestData(uuid, it.dimension(), blockPos)
            UUIDManager.save()
        }
    }

    override fun load(tag: CompoundTag) {
        if (tag.hasUUID("UUID"))
            uuid = tag.getUUID("UUID")
    }

    override fun saveAdditional(tag: CompoundTag) {
        tag.putUUID("UUID", uuid)
    }

    fun startOpen(player: Player) {
        if (remove || player.isSpectator || playerCache.contains(player)) return
        playerCache.add(player)
        level?.let { openersCounter.incrementOpeners(player, it, blockPos, blockState) }
    }

    fun checkCache() {
        if (remove) return
        playerCache.forEach {
            if (it.isSpectator) return@forEach
            stopOpen(it)
        }
        playerCache.clear()
    }

    private fun stopOpen(player: Player) {
        if (remove || player.isSpectator) return
        ChestLevelManager.setActive(uuid)
        level?.let { openersCounter.decrementOpeners(player, it, blockPos, blockState) }
        (level as? ServerLevel)?.let {
            val world = ChestLevelManager.getOrCreate(Constants.Server, uuid)
            UUIDManager.setChestData(uuid, it.dimension(), blockPos)
            UUIDManager.save()

            (player as? ServerPlayer)?.let { serverPlayer ->
                serverPlayer.openedChestDimension = it
                serverPlayer.openedChestPos = blockPos
            }

            ChestLevelManager.teleportEntityToEnter(world, player)
        }
    }

    fun recheckOpen() {
        if (remove) return
        level?.let { openersCounter.recheckOpeners(it, blockPos, blockState) }
    }

    override fun getOpenNess(partialTicks: Float): Float {
        return chestLidController.getOpenness(partialTicks)
    }
}