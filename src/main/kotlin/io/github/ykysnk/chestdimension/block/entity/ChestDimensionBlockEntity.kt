package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ChestLidController
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.minecraft.world.level.block.entity.LidBlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class ChestDimensionBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.CHEST_DIMENSION, pos, blockState), LidBlockEntity {
    companion object {
        private val damageSourceType: Holder.Reference<DamageType> by lazy {
            Constants.Server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.EXPLOSION_BY_CHEST)
        }

        private val damageSourceInsideType: Holder.Reference<DamageType> by lazy {
            Constants.Server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.EXPLOSION_BY_CHEST_INSIDE)
        }

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

    var destroyByItSelf: Boolean = false
        private set

    private val playerCache: HashSet<Player> = hashSetOf()

    override fun triggerEvent(id: Int, type: Int): Boolean {
        if (id == 1) {
            chestLidController.shouldBeOpen(type > 0)
            return true
        } else {
            return super.triggerEvent(id, type)
        }
    }

    fun haveSameChest(): Boolean {
        val exitDimKey = UUIDManager.getExitChestDimensionKey(uuid)
        val exitPos = UUIDManager.getExitChestPosition(uuid)
        return exitDimKey != null && exitPos != null && (exitDimKey != level?.dimension() || exitPos != blockPos)
    }

    private fun isChestInsideChest(): Boolean = (level as? ServerLevel)?.let {
        val levelUUID = ChestLevelManager.findUUIDByLevel(it)
        return levelUUID == uuid
    } ?: false

    private fun explodeItSelf(level: ServerLevel): Boolean {
        val isInsideChest = isChestInsideChest()
        if (!haveSameChest() && !isInsideChest) return false
        val damageSourcePosition = blockPos.center
        val damageSource =
            DamageSource(if (isInsideChest) damageSourceInsideType else damageSourceType, damageSourcePosition)
        destroyByItSelf = true
        val drops = Block.getDrops(blockState, level, blockPos, this)
        level.removeBlock(blockPos, false)
        level.explode(
            null,
            damageSource,
            null,
            damageSourcePosition,
            if (isInsideChest) 10f else 6f,
            true,
            Level.ExplosionInteraction.BLOCK
        )
        drops.forEach { Block.popResource(level, blockPos, it) }
        return true
    }

    override fun setLevel(level: Level) {
        super.setLevel(level)
        (level as? ServerLevel)?.apply {
            ChestLevelManager.setActive(uuid)
            if (haveSameChest()) return@apply
            UUIDManager.setChestData(uuid, dimension(), blockPos)
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
            if (explodeItSelf(it)) return@let
            val world = ChestLevelManager.getOrCreate(Constants.Server, uuid)
            UUIDManager.setChestData(uuid, it.dimension(), blockPos)
            UUIDManager.save()
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