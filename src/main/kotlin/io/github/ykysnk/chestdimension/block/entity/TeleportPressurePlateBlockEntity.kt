package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.level.ChestLevelManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TeleportPressurePlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_PRESSURE_PLATE, pos, blockState) {
    private val entitiesCache: HashSet<Entity> = HashSet()

    override fun load(tag: CompoundTag) {
    }

    override fun saveAdditional(tag: CompoundTag) {
    }

    fun addEntity(entity: Entity) {
        entitiesCache.add(entity)
    }

    fun teleport() {
        level?.let { currentLevel ->
            if (!ChestLevelManager.teleportEntitiesToExit(currentLevel, entitiesCache.toList())) {
                // TODO
            }
        }
        entitiesCache.clear()
    }
}