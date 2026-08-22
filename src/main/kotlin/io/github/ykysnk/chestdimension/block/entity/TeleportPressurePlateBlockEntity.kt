package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.extensions.findStandUpPosition
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.extensions.teleportToSafeLocation
import io.github.ykysnk.chestdimension.extensions.teleportToSpawnLocation
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TeleportPressurePlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_PRESSURE_PLATE, pos, blockState) {
    companion object {
        private val TELEPORT_HORIZONTAL_OFFSETS: List<Vec3i> = listOf(
            Vec3i(0, 0, -1),
            Vec3i(-1, 0, 0),
            Vec3i(0, 0, 1),
            Vec3i(1, 0, 0),
            Vec3i(-1, 0, -1),
            Vec3i(1, 0, -1),
            Vec3i(-1, 0, 1),
            Vec3i(1, 0, 1)
        )
        private val TELEPORT_OFFSETS: List<Vec3i> = buildList {
            addAll(TELEPORT_HORIZONTAL_OFFSETS)
            addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.below() })
            addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.above() })
            add(Vec3i(0, 1, 0))
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
                val exitPos = UUIDManager.getExitChestPos(it)

                if (exitDim != null && exitPos != null) {
                    entitiesCache.forEach { entity ->
                        val safePos = entity.findStandUpPosition(exitDim, exitPos, TELEPORT_OFFSETS)
                        if (safePos != null)
                            entity.teleportToLevel(exitDim, safePos)
                        else {
                            val test = entity.teleportToSafeLocation(exitDim, exitPos)
                            Constants.LOGGER.info("Teleporting to safe location: $test")
                            if (!test)
                                entity.teleportToSpawnLocation(overworld)
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