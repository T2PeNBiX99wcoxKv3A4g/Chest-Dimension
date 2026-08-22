package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.extensions.*
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.abs

class TeleportPressurePlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_PRESSURE_PLATE, pos, blockState) {
    companion object {
        private val TELEPORT_HORIZONTAL_OFFSETS: List<Vec3i> = (-2..2)
            .flatMap { x -> (-2..2).map { z -> Vec3i(x, 0, z) } }
            .filter { it.x != 0 || it.z != 0 }
            .sortedBy { maxOf(abs(it.x), abs(it.z)) }
        private val TELEPORT_OFFSETS: List<Vec3i> = buildList {
            addAll(TELEPORT_HORIZONTAL_OFFSETS)
            for (i in 1..5) {
                addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.below(i) })
                addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.above(i) })
                add(Vec3i(0, i, 0))
            }
        }
    }

    val entitiesCache: HashSet<Entity> = HashSet()

    override fun load(tag: CompoundTag) {
    }

    override fun saveAdditional(tag: CompoundTag) {
    }

    fun addEntity(entity: Entity) {
        entitiesCache.add(entity)
    }

    fun teleport() {
        level?.let { currentLevel ->
            val overworld = Constants.Server.overworld()
            val uuid = ChestLevelManager.findUUIDByLevel(currentLevel)
            uuid?.let {
                val exitDim = UUIDManager.getExitChestDimension(it)
                val exitPos = UUIDManager.getExitChestPosition(it)

                if (exitDim != null && exitPos != null) {
                    entitiesCache.forEach { entity ->
                        val safePos = entity.findStandUpPosition(exitDim, exitPos, TELEPORT_OFFSETS)
                        if (safePos != null)
                            entity.teleportToLevel(exitDim, safePos)
                        else {
                            val topPos = entity.findChestTopPosition(exitDim, exitPos)
                            if (topPos != null) entity.teleportToLevel(exitDim, topPos)
                            else {
                                if (!entity.teleportToSafeLocation(exitDim, exitPos))
                                    entity.teleportToSpawnLocation(overworld)
                            }
                        }
                    }
                } else {
                    entitiesCache.forEach { entity -> entity.teleportToSpawnLocation(overworld) }
                }
            } ?: run {
                // TODO
            }
        }
        entitiesCache.clear()
    }
}