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

    private fun add(uuid: UUID, level: Level, blockPos: BlockPos) {
        if (level.isClientSide) return
        add(uuid, Constants.Server.worldData.levelName, DimensionPosition(level.dimension(), blockPos))
    }

    private fun add(uuid: UUID, levelName: String, dimensionPosition: DimensionPosition) {
        data.points.getOrPut(uuid.toString()) { hashMapOf() }[levelName] =
            TeleportInfo(dimensionPosition = dimensionPosition)
    }

    operator fun set(uuid: UUID, level: Level, blockPos: BlockPos) {
        if (level.isClientSide) return
        removePosition(uuid)
        getTeleportInfo(uuid)?.let {
            setTeleportInfo(
                uuid,
                Constants.Server.worldData.levelName,
                it.copy(dimensionPosition = DimensionPosition(level.dimension(), blockPos))
            )
        } ?: run {
            add(uuid, Constants.Server.worldData.levelName, DimensionPosition(level.dimension(), blockPos))
        }
    }

    operator fun get(uuid: UUID) = data.points[uuid.toString()]

    fun getPosition(uuid: UUID) = getPosition(uuid, Constants.Server.worldData.levelName)

    fun getPosition(uuid: UUID, levelName: String) = get(uuid)?.get(levelName)?.dimensionPosition

    fun getTeleportInfo(uuid: UUID) = getTeleportInfo(uuid, Constants.Server.worldData.levelName)

    fun getTeleportInfo(uuid: UUID, levelName: String) = get(uuid)?.get(levelName)

    fun setTeleportInfo(uuid: UUID, teleportInfo: TeleportInfo) =
        setTeleportInfo(uuid, Constants.Server.worldData.levelName, teleportInfo)

    fun setTeleportInfo(uuid: UUID, levelName: String, teleportInfo: TeleportInfo) {
        data.points.getOrPut(uuid.toString()) { hashMapOf() }[levelName] = teleportInfo
        if (data.points.getOrPut(uuid.toString()) { hashMapOf() }[levelName]!!.isEmpty()) removeLevel(uuid, levelName)
    }

    fun isExist(uuid: UUID) = get(uuid) != null

    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) = data.points.remove(uuidString)

    fun removeLevel(uuid: UUID) = removeLevel(uuid, Constants.Server.worldData.levelName)

    fun removeLevel(uuid: UUID, levelName: String): TeleportInfo? {
        val info = get(uuid)?.remove(levelName)
        if (get(uuid)?.keys?.isEmpty() == true) remove(uuid)
        return info
    }

    fun removePosition(uuid: UUID) = removePosition(uuid, Constants.Server.worldData.levelName)

    fun removePosition(uuid: UUID, levelName: String): TeleportInfo? {
        val info = getTeleportInfo(uuid, levelName) ?: return removeLevel(uuid, levelName)
        if (info.isEmpty()) return removeLevel(uuid, levelName)
        setTeleportInfo(uuid, levelName, info.copy(dimensionPosition = null))
        return info
    }

    fun linkDoors(first: UUID, second: UUID) = linkDoors(first, second, Constants.Server.worldData.levelName)

    fun linkDoors(first: UUID, second: UUID, levelName: String): Boolean {
        if (first == second) return false
        getTeleportInfo(first, levelName)?.let { firstInfo ->
            getTeleportInfo(second, levelName)?.let { secondInfo ->
                unLinkDoors(first, levelName)
                unLinkDoors(second, levelName)
                setTeleportInfo(first, levelName, firstInfo.copy(linkUUID = second))
                setTeleportInfo(second, levelName, secondInfo.copy(linkUUID = first))
                return true
            }
        }
        return false
    }

    fun unLinkDoors(uuid: UUID, levelName: String) {
        getTeleportInfo(uuid, levelName)?.let {
            setTeleportInfo(uuid, levelName, it.copy(linkUUID = null))
        }
    }

    fun isLinkDoor(uuid: UUID) = isLinkDoor(uuid, Constants.Server.worldData.levelName)

    fun isLinkDoor(uuid: UUID, levelName: String) = get(uuid)?.get(levelName)?.linkUUID != null

    override fun containsUUID(uuid: UUID) = data.points.containsKey(uuid.toString())
}