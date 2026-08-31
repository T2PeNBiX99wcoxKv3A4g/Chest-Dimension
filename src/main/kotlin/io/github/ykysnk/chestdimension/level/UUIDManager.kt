package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.data.DimensionPosition
import io.github.ykysnk.chestdimension.level.data.LevelData
import io.github.ykysnk.chestdimension.level.data.Levels
import io.github.ykysnk.chestdimension.level.data.RandomUUID
import io.github.ykysnk.chestdimension.utils.AbstractManager
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import java.io.File
import java.nio.file.Files
import java.util.*

object UUIDManager : AbstractManager<Levels>("levels.dat", Levels()), RandomUUID {
    override fun load() {
        if (!Files.exists(dataPath)) {
            saveNow()
            return
        }

        val tag = NbtIo.readCompressed(File(dataPath.toUri()))
        data = Levels.load(tag)
    }

    override fun save() {
        scope.launch {
            val snapshot = data.deepCopy()
            saveNow(snapshot)
        }
    }

    override fun saveNow(saveData: Levels) {
        NbtIo.writeCompressed(saveData.save(), File(dataPath.toUri()))
    }

    fun add(uuid: UUID, seed: Long) {
        data.levels[uuid.toString()] = LevelData(seed)
    }

    @Suppress("unused")
    fun haveChestData(uuid: UUID) = haveChestData(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun haveChestData(uuid: UUID, levelName: String): Boolean =
        data.levels[uuid.toString()]?.let { levels -> levels.dimensionPositions[levelName] != null } ?: false

    fun setChestData(uuid: UUID, key: ResourceKey<Level>, pos: BlockPos) =
        setChestData(uuid, Constants.Server.worldData.levelName, key, pos)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setChestData(uuid: UUID, levelName: String, key: ResourceKey<Level>, pos: BlockPos) {
        val uuidString = uuid.toString()
        data.levels[uuidString]?.dimensionPositions[levelName] = DimensionPosition(key, pos)
    }

    fun clearChestData(uuid: UUID) = clearChestData(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun clearChestData(uuid: UUID, levelName: String): DimensionPosition? {
        val uuidString = uuid.toString()
        return data.levels[uuidString]?.dimensionPositions?.remove(levelName)
    }

    fun isActive(uuid: UUID) = isActive(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun isActive(uuid: UUID, levelName: String): Boolean {
        val list = data.inactiveLevels[levelName] ?: return true
        return !list.contains(uuid.toString())
    }

    fun setInactive(uuid: UUID) = setInactive(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setInactive(uuid: UUID, levelName: String) {
        data.inactiveLevels.getOrPut(levelName) { hashSetOf() }.add(uuid.toString())
    }

    fun setActive(uuid: UUID) = setActive(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setActive(uuid: UUID, levelName: String) =
        data.inactiveLevels.getOrPut(levelName) { hashSetOf() }.remove(uuid.toString())

    fun isExist(uuid: UUID) = data.levels[uuid.toString()] != null

    fun getExitChestPosition(uuid: UUID): BlockPos? = getExitChestPosition(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestPosition(uuid: UUID, levelName: String): BlockPos? {
        val data = data.levels[uuid.toString()] ?: return null
        return data.dimensionPositions[levelName]?.blockPos
    }

    fun getExitChestDimensionKey(uuid: UUID): ResourceKey<Level>? =
        getExitChestDimensionKey(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestDimensionKey(uuid: UUID, levelName: String): ResourceKey<Level>? =
        data.levels[uuid.toString()]?.dimensionPositions[levelName]?.dimension

    fun getExitChestDimension(uuid: UUID): ServerLevel? =
        getExitChestDimension(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestDimension(uuid: UUID, levelName: String): ServerLevel? =
        data.levels[uuid.toString()]?.dimensionPositions[levelName]?.dimension?.let(Constants.Server::getLevel)

    operator fun set(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    operator fun get(uuid: UUID) = data.levels[uuid.toString()]

    @Suppress("unused")
    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) = data.levels.remove(uuidString)

    fun getMap() = data.levels.toMap()

    fun getInactiveList() = getInactiveList(Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getInactiveList(levelName: String) = data.inactiveLevels[levelName]?.toSet() ?: setOf()

    override fun randomUUID(): UUID {
        while (true) {
            val uuid = UUID.randomUUID()
            if (!data.levels.containsKey(uuid.toString())) return uuid
        }
    }
}