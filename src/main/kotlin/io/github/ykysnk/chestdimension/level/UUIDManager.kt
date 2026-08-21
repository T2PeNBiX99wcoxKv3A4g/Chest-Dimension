package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.mamoe.yamlkt.Yaml
import java.nio.file.Files
import java.util.*
import kotlin.io.path.readText
import kotlin.io.path.writeText

object UUIDManager {
    private val dataPath = Constants.ConfigDir.resolve("levels.yaml")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var data: Levels = Levels()

    fun load() {
        if (!Files.exists(dataPath)) {
            saveNow(data)
            return
        }
        data = Yaml.decodeFromString(dataPath.readText())
    }

    fun save() {
        val snapshot = data.deepCopy()
        scope.launch { saveNow(snapshot) }
    }

    private fun saveNow(levels: Levels) {
        val text = Yaml.encodeToString(levels)
        dataPath.writeText(text)
    }

    fun add(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    fun get() = data.levels.toMap()

    init {
        load()
        ServerLifecycleEvents.SERVER_STOPPING.register {
            saveNow(data)
            scope.cancel()
        }
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
            save()
        }
    }
}