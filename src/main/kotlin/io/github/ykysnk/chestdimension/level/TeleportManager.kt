@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.data.DimensionPosition
import io.github.ykysnk.chestdimension.level.data.RandomUUID
import io.github.ykysnk.chestdimension.level.data.TeleportInfo
import io.github.ykysnk.chestdimension.level.data.TeleportPoints
import io.github.ykysnk.chestdimension.utils.AbstractManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.io.File
import java.util.*

object TeleportManager : AbstractManager<TeleportPoints>("teleport_points.dat", TeleportPoints()), RandomUUID {
    override fun loadData(): TeleportPoints {
        val tag = NbtIo.readCompressed(File(dataPath.toUri()))
        return TeleportPoints.load(tag)
    }

    override fun saveNow(saveData: TeleportPoints) {
        NbtIo.writeCompressed(saveData.save(), File(dataPath.toUri()))
    }

    private fun getLevelName(levelKey: ResourceKey<Level>): String =
        if (ChestLevelManager.isInsideChestDimension(levelKey)) Constants.MOD_ID else Constants.Server.worldData.levelName

    private fun getLevelName(dimensionPosition: DimensionPosition): String = getLevelName(dimensionPosition.dimension)

    private fun getLevelName(level: Level) = getLevelName(level.dimension())

    fun add(uuid: UUID, level: Level, blockPos: BlockPos) {
        if (level.isClientSide) return
        add(uuid, getLevelName(level.dimension()), DimensionPosition(level.dimension(), blockPos))
    }

    fun add(uuid: UUID, levelName: String, dimensionPosition: DimensionPosition) {
        data.points.getOrPut(uuid.toString()) { hashMapOf() }[levelName] = TeleportInfo(dimensionPosition)
    }

    operator fun set(uuid: UUID, level: Level, blockPos: BlockPos) =
        add(uuid, getLevelName(level.dimension()), DimensionPosition(level.dimension(), blockPos))

    operator fun get(uuid: UUID) = data.points[uuid.toString()]

    fun isExist(uuid: UUID) = get(uuid) != null

    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) = data.points.remove(uuidString)

    fun removePosition(uuid: UUID, level: Level): TeleportInfo? {
        if (level.isClientSide) return null
        return get(uuid)?.remove(getLevelName(level))
    }

    fun removePosition(uuid: UUID, levelName: String) = get(uuid)?.remove(levelName)

    override fun containsUUID(uuid: UUID) = data.points.containsKey(uuid.toString())
}