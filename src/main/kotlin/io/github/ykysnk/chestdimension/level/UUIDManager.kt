package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import kotlinx.serialization.decodeFromString
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.mamoe.yamlkt.Yaml
import java.nio.file.Files
import java.util.*
import kotlin.io.path.readText
import kotlin.io.path.writeText

object UUIDManager {
    private val dataPath = Constants.ConfigDir.resolve("levels.yaml")

    var data: Levels = Levels()
        private set

    fun load() {
        if (!Files.exists(dataPath)) {
            save()
            return
        }
        data = Yaml.decodeFromString(dataPath.readText())
    }

    fun save() {
        val data = Yaml.encodeToString(data)
        dataPath.writeText(data)
    }

    fun add(uuid: UUID, levelData: LevelData) {
        data.levels[uuid.toString()] = levelData
    }

    fun get() = data.levels

    init {
        load()
        ServerLifecycleEvents.SERVER_STOPPING.register {
            save()
        }
    }
}