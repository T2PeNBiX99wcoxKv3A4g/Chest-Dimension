package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.data.BlockPosData.Companion.toData
import io.github.ykysnk.chestdimension.data.ChestDimensionPosition
import io.github.ykysnk.chestdimension.data.DimensionData.Companion.toData
import io.github.ykysnk.chestdimension.data.LevelData
import io.github.ykysnk.chestdimension.data.Levels
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.mamoe.yamlkt.Yaml
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import java.nio.file.Files
import java.util.*
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

object UUIDManager {
    private val dataPath = Constants.ConfigDir.resolve("levels.yaml")
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var data: Levels = Levels()

    private fun load() {
        if (!Files.exists(dataPath)) {
            saveNow()
            return
        }
        data = Yaml.decodeFromString(dataPath.readText())
    }

    fun save() {
        scope.launch {
            val snapshot = data.deepCopy()
            saveNow(snapshot)
        }
    }

    fun saveNow() = saveNow(data)

    private fun saveNow(levels: Levels) {
        val text = Yaml.encodeToString(levels)
        dataPath.writeText(text)
    }

    fun add(uuid: UUID, seed: Long) {
        data.levels[uuid.toString()] = LevelData(seed)
    }

    @Suppress("unused")
    fun haveChestData(uuid: UUID) = haveChestData(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun haveChestData(uuid: UUID, levelName: String): Boolean = data.levels[uuid.toString()]?.let { levels ->
        val list = levels.chestDimensionPositions[levelName]
        return list?.dimension != null && list.blockPos != null
    } ?: false

    fun setChestData(uuid: UUID, key: ResourceKey<Level>, pos: BlockPos) =
        setChestData(uuid, Constants.Server.worldData.levelName, key, pos)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setChestData(uuid: UUID, levelName: String, key: ResourceKey<Level>, pos: BlockPos) {
        val uuidString = uuid.toString()
        data.levels[uuidString]?.chestDimensionPositions[levelName] = ChestDimensionPosition(key.toData(), pos.toData())
    }

    fun clearChestData(uuid: UUID) = clearChestData(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun clearChestData(uuid: UUID, levelName: String) {
        val uuidString = uuid.toString()
        data.levels[uuidString]?.chestDimensionPositions?.remove(levelName)
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
        if (!data.inactiveLevels.containsKey(levelName))
            data.inactiveLevels[levelName] = hashSetOf()
        data.inactiveLevels[levelName]?.add(uuid.toString())
    }

    fun setActive(uuid: UUID) = setActive(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setActive(uuid: UUID, levelName: String) {
        if (!data.inactiveLevels.containsKey(levelName))
            data.inactiveLevels[levelName] = hashSetOf()
        data.inactiveLevels[levelName]?.remove(uuid.toString())
    }

    fun getExitChestPosition(uuid: UUID): BlockPos? = getExitChestPosition(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestPosition(uuid: UUID, levelName: String): BlockPos? {
        val data = data.levels[uuid.toString()] ?: return null
        return data.chestDimensionPositions[levelName]?.blockPos?.blockPos
    }

    fun getExitChestDimensionKey(uuid: UUID): ResourceKey<Level>? =
        getExitChestDimensionKey(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestDimensionKey(uuid: UUID, levelName: String): ResourceKey<Level>? =
        data.levels[uuid.toString()]?.chestDimensionPositions[levelName]?.dimension?.resourceKey

    fun getExitChestDimension(uuid: UUID): ServerLevel? =
        getExitChestDimension(uuid, Constants.Server.worldData.levelName)

    @Suppress("MemberVisibilityCanBePrivate")
    fun getExitChestDimension(uuid: UUID, levelName: String): ServerLevel? =
        data.levels[uuid.toString()]?.chestDimensionPositions[levelName]?.dimension?.resourceKey?.let(Constants.Server::getLevel)

    operator fun set(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    operator fun get(uuid: UUID) = data.levels[uuid.toString()]

    @Suppress("unused")
    fun remove(uuid: UUID) = remove(uuid.toString())

    fun remove(uuidString: String) {
        data.levels.remove(uuidString)
    }

    fun getMap() = data.levels.toMap()

    fun getInactiveMap() = data.inactiveLevels.mapValues { (_, value) -> value.toSet() }

    fun randomUUID(): UUID {
        while (true) {
            val uuid = UUID.randomUUID()
            if (!data.levels.containsKey(uuid.toString())) return uuid
        }
    }

    init {
        load()
        ServerLifecycleEvents.SERVER_STARTING.register {
            scope.launch {
                while (isActive) {
                    delay(5.minutes)
                    save()
                }
            }
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            saveNow()
            scope.cancel()
        }
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
            save()
        }
    }
}