package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.level.TeleportManager
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
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

class TeleportDoorBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_DOOR, pos, blockState) {
    companion object {
        private val damageSourceType: Holder.Reference<DamageType> by lazy {
            Constants.Server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.EXPLOSION_BY_TELEPORT_DOOR)
        }

        private fun explodePush(level: ServerLevel, pos: BlockPos, entity: Entity) {
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
        }
    }

    var uuid: UUID = TeleportManager.randomUUID()
        private set

    private val entitiesCache = hashSetOf<Entity>()

    fun enterDoor(entity: Entity) {
        entitiesCache.add(entity)
    }

    fun startTeleport() {
        (level as? ServerLevel)?.apply { entitiesCache.forEach { handleTeleport(this, it) } }
        entitiesCache.clear()
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
        val teleportFacing = teleportState.getValue(FACING)
        val teleportPlayerPos = when (teleportFacing) {
            Direction.NORTH -> teleportPos.south()
            Direction.SOUTH -> teleportPos.north()
            Direction.EAST -> teleportPos.west()
            Direction.WEST -> teleportPos.east()
            else -> teleportPos
        }
        val teleportPlayerDirection = when (teleportFacing) {
            Direction.NORTH -> Direction.SOUTH
            Direction.SOUTH -> Direction.NORTH
            Direction.EAST -> Direction.WEST
            Direction.WEST -> Direction.EAST
            else -> Direction.NORTH
        }
        entity.teleportToLevel(teleportLevel, teleportPlayerPos, teleportPlayerDirection)
    }

    override fun setLevel(level: Level) {
        super.setLevel(level)
        (level as? ServerLevel)?.apply {
            if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) return@apply
            TeleportManager[uuid, this] = blockPos
            TeleportManager.save()
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