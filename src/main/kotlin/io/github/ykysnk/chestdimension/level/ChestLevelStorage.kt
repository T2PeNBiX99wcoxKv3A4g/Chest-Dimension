package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.world.level.storage.LevelStorageSource
import java.nio.file.Files
import java.nio.file.Path

object ChestLevelStorage {
    init {
        ServerLifecycleEvents.SERVER_STARTED.register {
            initialize()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            clear()
        }
    }

    private val root: Path = Constants.ConfigDir

    private lateinit var storageSource: LevelStorageSource

    private fun initialize() {
        if (::storageSource.isInitialized) return
        Files.createDirectories(root)
        storageSource = LevelStorageSource.createDefault(root)
    }

    private fun source(): LevelStorageSource {
        if (!::storageSource.isInitialized)
            initialize()
        return storageSource
    }

    private fun clear() {
        access.close()
    }

    val access: LevelStorageSource.LevelStorageAccess by lazy { source().createAccess("save") }
}