package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.data.BlockPosData.Companion.toData
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
    private var scope: CoroutineScope? = null
    private var data: Levels = Levels()

    private fun load() {
        if (!Files.exists(dataPath)) {
            saveNow()
            return
        }
        data = Yaml.decodeFromString(dataPath.readText())
    }

    fun save() {
        scope?.launch {
            val snapshot = data.deepCopy()
            saveNow(snapshot)
        }
    }

    private fun saveNow() = saveNow(data)

    private fun saveNow(levels: Levels) {
        val text = Yaml.encodeToString(levels)
        dataPath.writeText(text)
    }

    fun add(uuid: UUID, seed: Long) {
        data.levels[uuid.toString()] = LevelData(seed)
    }

    fun setChestData(uuid: UUID, key: ResourceKey<Level>, pos: BlockPos) {
        val oldData = data.levels[uuid.toString()] ?: LevelData(-1)
        data.levels[uuid.toString()] = oldData.copy(chestDimension = key.toData(), chestPos = pos.toData())
    }

    fun clearChestData(uuid: UUID) {
        val oldData = data.levels[uuid.toString()] ?: LevelData(-1)
        data.levels[uuid.toString()] = oldData.copy(chestDimension = null, chestPos = null)
    }

    fun isActive(uuid: UUID) = !data.inactiveLevels.contains(uuid.toString())

    fun setInactive(uuid: UUID) {
        data.inactiveLevels.add(uuid.toString())
    }

    fun setActive(uuid: UUID) {
        data.inactiveLevels.remove(uuid.toString())
    }

    fun getExitChestPosition(uuid: UUID): BlockPos? {
        val data = data.levels[uuid.toString()] ?: return null
        return data.chestPos?.blockPos
    }

    fun getExitChestDimension(uuid: UUID): ServerLevel? =
        data.levels[uuid.toString()]?.chestDimension?.resourceKey?.let(Constants.Server::getLevel)

    operator fun set(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    operator fun get(uuid: UUID) = data.levels[uuid.toString()]

    fun getMap() = data.levels.toMap()

    fun randomUUID(): UUID {
        while (true) {
            val uuid = UUID.randomUUID()
            if (!data.levels.containsKey(uuid.toString())) return uuid
        }
    }

    init {
        load()
        ServerLifecycleEvents.SERVER_STARTING.register {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope?.launch {
                while (isActive) {
                    delay(5.minutes)
                    save()
                }
            }
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            saveNow()
            scope?.cancel()
        }
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
            save()
        }
    }
}