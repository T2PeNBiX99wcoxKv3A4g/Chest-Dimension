package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.TeleportDoorBlock
import io.github.ykysnk.chestdimension.config.Configs
import io.github.ykysnk.chestdimension.extensions.findSafeLocation
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.extensions.teleportToSafeLocation
import io.github.ykysnk.chestdimension.extensions.teleportToSpawnLocation
import io.github.ykysnk.chestdimension.level.TeleportManager
import io.github.ykysnk.chestdimension.level.UndefinedLevelManager
import io.github.ykysnk.chestdimension.utils.TaskPool
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.DoorBlock.FACING
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import java.util.*
import kotlin.math.abs

class TeleportDoorBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_DOOR, pos, blockState) {
    companion object {
        private val TELEPORT_HORIZONTAL_OFFSETS: List<Vec3i> = (-2..2)
            .flatMap { x -> (-2..2).map { z -> Vec3i(x, 0, z) } }
            .sortedBy { maxOf(abs(it.x), abs(it.z)) }
        private val TELEPORT_OFFSETS: List<Vec3i> = buildList {
            addAll(TELEPORT_HORIZONTAL_OFFSETS)
            for (i in 1..5) {
                addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.below(i) })
                addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.above(i) })
            }
        }
        private val damageSourceType: Holder.Reference<DamageType> by lazy {
            Constants.Server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.EXPLOSION_BY_TELEPORT_DOOR)
        }

        val blockPosList = mutableListOf<BlockPos>()
    }

    var uuid: UUID = TeleportManager.randomUUID()
        private set

    private val entitiesCache = hashSetOf<Entity>()

    fun enterDoor(entity: Entity) {
        entitiesCache.add(entity)
    }

    fun isInDoor(entity: Entity): Boolean {
        return entitiesCache.contains(entity)
    }

    fun startTeleport() {
        (level as? ServerLevel)?.apply { entitiesCache.forEach { handleTeleport(this, it) } }
        entitiesCache.clear()
    }

    private fun explodePush(level: ServerLevel, pos: BlockPos, entity: Entity) {
        if (!Configs.mainConfig.teleportDoorTeleportToUndefined) {
            val damageSource = DamageSource(damageSourceType)
            level.explode(
                null,
                damageSource,
                null,
                pos.center,
                .5f,
                false,
                Level.ExplosionInteraction.NONE
            )
            return
        }
        val undefinedLevel = UndefinedLevelManager.getOrCreate(Constants.Server)
        val block = blockState.block
        (block as? TeleportDoorBlock)?.apply { setOpen(null, level, blockState, blockPos, false) }
        entity.teleportToSpawnLocation(undefinedLevel)
    }

    private fun handleTeleport(level: ServerLevel, entity: Entity) {
        if (!TeleportManager.isLinkDoor(uuid)) {
            explodePush(level, worldPosition, entity)
            return
        }
        val info = TeleportManager.getTeleportInfo(uuid)
        if (info?.linkUUID == null) {
            explodePush(level, worldPosition, entity)
            return
        }
        val teleportToInfo = TeleportManager.getTeleportInfo(info.linkUUID)
        if (teleportToInfo?.dimensionPosition == null) {
            explodePush(level, worldPosition, entity)
            return
        }
        val teleportLevel = level.server.getLevel(teleportToInfo.dimensionPosition.dimension)
        if (teleportLevel == null) {
            explodePush(level, worldPosition, entity)
            return
        }
        val teleportPos = teleportToInfo.dimensionPosition.blockPos
        val teleportState = teleportLevel.getBlockState(teleportPos)
        if (!teleportState.`is`(Blocks.TELEPORT_DOOR)) {
            explodePush(level, worldPosition, entity)
            return
        }
        val sourceFacing = blockState.getValue(FACING)
        val teleportFacing = teleportState.getValue(FACING)
        val teleportPlayerPos = when (teleportFacing) {
            Direction.NORTH -> teleportPos.south()
            Direction.SOUTH -> teleportPos.north()
            Direction.EAST -> teleportPos.west()
            Direction.WEST -> teleportPos.east()
            else -> teleportPos
        }
        val facingOffset = Mth.wrapDegrees(teleportFacing.toYRot() - sourceFacing.toYRot())
        val entityNewYRot = Mth.wrapDegrees(entity.yRot + 180f + facingOffset)
        val block = blockState.block
        (block as? TeleportDoorBlock)?.apply { setOpen(null, level, blockState, blockPos, false) }
        entity.setPortalCooldown()
        entity.findSafeLocation(teleportLevel, teleportPlayerPos, TELEPORT_OFFSETS)?.let { safePos ->
            if (entity.teleportToLevel(teleportLevel, safePos, entityNewYRot, entity.xRot)) return
        }
        if (entity.teleportToSafeLocation(teleportLevel, teleportPlayerPos)) return
        explodePush(level, worldPosition, entity)
    }

    override fun setLevel(level: Level) {
        super.setLevel(level)
        blockPosList.add(blockPos)
        (level as? ServerLevel)?.apply {
            TaskPool.run {
                if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                    val pos = blockPos.below()
                    val blockEntity = getBlockEntity(pos)
                    (blockEntity as? TeleportDoorBlockEntity)?.let { uuid = it.uuid }
                    return@run
                }

                TeleportManager[uuid, this] = blockPos
                TeleportManager.save()
            }
        }
    }

    override fun load(tag: CompoundTag) {
        if (tag.hasUUID("UUID"))
            uuid = tag.getUUID("UUID")
    }

    override fun saveAdditional(tag: CompoundTag) {
        if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
            (level as? ServerLevel)?.apply {
                val pos = blockPos.below()
                val belowEntity = getBlockEntity(pos)
                (belowEntity as? TeleportDoorBlockEntity)?.let { uuid = it.uuid }
            }
        }
        tag.putUUID("UUID", uuid)
    }
}