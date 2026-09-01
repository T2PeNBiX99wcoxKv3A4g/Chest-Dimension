@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.level.data.DimensionPosition
import io.github.ykysnk.chestdimension.level.data.RandomUUID
import io.github.ykysnk.chestdimension.level.data.TeleportInfo
import io.github.ykysnk.chestdimension.level.data.TeleportPoints
import io.github.ykysnk.chestdimension.utils.AbstractManager
import net.minecraft.nbt.NbtIo
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

    fun add(uuid: UUID, levelName: String, dimensionPosition: DimensionPosition) {
        data.points.getOrPut(uuid.toString()) { hashMapOf() }[levelName] = TeleportInfo(dimensionPosition)
    }

    operator fun get(uuid: UUID) = data.points[uuid.toString()]

    fun isExist(uuid: UUID) = get(uuid) != null

    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) = data.points.remove(uuidString)

    override fun containsUUID(uuid: UUID) = data.points.containsKey(uuid.toString())
}