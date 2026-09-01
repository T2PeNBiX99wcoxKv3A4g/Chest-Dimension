@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.data.DimensionPosition
import io.github.ykysnk.chestdimension.level.data.LevelData
import io.github.ykysnk.chestdimension.level.data.Levels
import io.github.ykysnk.chestdimension.level.data.RandomUUID
import io.github.ykysnk.chestdimension.utils.AbstractManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import java.io.File
import java.util.*

object UUIDManager : AbstractManager<Levels>("levels.dat", Levels()), RandomUUID {
    override fun loadData(): Levels {
        val tag = NbtIo.readCompressed(File(dataPath.toUri()))
        return Levels.load(tag)
    }

    override fun saveNow(saveData: Levels) {
        NbtIo.writeCompressed(saveData.save(), File(dataPath.toUri()))
    }

    fun add(uuid: UUID, seed: Long) {
        data.levels[uuid.toString()] = LevelData(seed)
    }

    fun haveChestData(uuid: UUID) = haveChestData(uuid, Constants.Server.worldData.levelName)

    fun haveChestData(uuid: UUID, levelName: String): Boolean =
        data.levels[uuid.toString()]?.let { levels -> levels.dimensionPositions[levelName] != null } ?: false

    fun setChestData(uuid: UUID, key: ResourceKey<Level>, pos: BlockPos) =
        setChestData(uuid, Constants.Server.worldData.levelName, key, pos)

    fun setChestData(uuid: UUID, levelName: String, key: ResourceKey<Level>, pos: BlockPos) {
        val uuidString = uuid.toString()
        data.levels[uuidString]?.dimensionPositions[levelName] = DimensionPosition(key, pos)
    }

    fun clearChestData(uuid: UUID) = clearChestData(uuid, Constants.Server.worldData.levelName)

    fun clearChestData(uuid: UUID, levelName: String): DimensionPosition? {
        val uuidString = uuid.toString()
        return data.levels[uuidString]?.dimensionPositions?.remove(levelName)
    }

    fun isActive(uuid: UUID) = isActive(uuid, Constants.Server.worldData.levelName)

    fun isActive(uuid: UUID, levelName: String): Boolean {
        val list = data.inactiveLevels[levelName] ?: return true
        return !list.contains(uuid.toString())
    }

    fun setInactive(uuid: UUID) = setInactive(uuid, Constants.Server.worldData.levelName)

    fun setInactive(uuid: UUID, levelName: String) {
        data.inactiveLevels.getOrPut(levelName) { hashSetOf() }.add(uuid.toString())
    }

    fun setActive(uuid: UUID) = setActive(uuid, Constants.Server.worldData.levelName)

    fun setActive(uuid: UUID, levelName: String) =
        data.inactiveLevels.getOrPut(levelName) { hashSetOf() }.remove(uuid.toString())

    fun isExist(uuid: UUID) = data.levels[uuid.toString()] != null

    fun getExitChestPosition(uuid: UUID): BlockPos? = getExitChestPosition(uuid, Constants.Server.worldData.levelName)

    fun getExitChestPosition(uuid: UUID, levelName: String): BlockPos? {
        val data = data.levels[uuid.toString()] ?: return null
        return data.dimensionPositions[levelName]?.blockPos
    }

    fun getExitChestDimensionKey(uuid: UUID): ResourceKey<Level>? =
        getExitChestDimensionKey(uuid, Constants.Server.worldData.levelName)

    fun getExitChestDimensionKey(uuid: UUID, levelName: String): ResourceKey<Level>? =
        data.levels[uuid.toString()]?.dimensionPositions[levelName]?.dimension

    fun getExitChestDimension(uuid: UUID): ServerLevel? =
        getExitChestDimension(uuid, Constants.Server.worldData.levelName)

    fun getExitChestDimension(uuid: UUID, levelName: String): ServerLevel? =
        data.levels[uuid.toString()]?.dimensionPositions[levelName]?.dimension?.let(Constants.Server::getLevel)

    operator fun set(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    operator fun get(uuid: UUID) = data.levels[uuid.toString()]

    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) = data.levels.remove(uuidString)

    fun getMap() = data.levels.toMap()

    fun getInactiveList() = getInactiveList(Constants.Server.worldData.levelName)

    fun getInactiveList(levelName: String) = data.inactiveLevels[levelName]?.toSet() ?: setOf()

    override fun containsUUID(uuid: UUID) = data.levels.containsKey(uuid.toString())
}