package io.github.ykysnk.chestdimension.level.storage

import io.github.ykysnk.chestdimension.Constants
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.world.level.storage.LevelStorageSource
import java.nio.file.Files
import java.nio.file.Path

object ChestLevelStorage {
    init {
        ServerLifecycleEvents.SERVER_STARTING.register {
            initialize()
        }
        ServerLifecycleEvents.SERVER_STOPPED.register {
            clear()
        }
    }

    private val root: Path = Constants.ConfigDir
    private var storageAccess: LevelStorageSource.LevelStorageAccess? = null
    val access: LevelStorageSource.LevelStorageAccess
        get() {
            if (storageAccess == null)
                initialize()
            return storageAccess ?: throw IllegalStateException("Storage access not initialized")
        }

    private val storageSource: LevelStorageSource by lazy {
        Files.createDirectories(root)
        LevelStorageSource.createDefault(root)
    }

    private fun initialize() {
        if (storageAccess != null) return
        storageAccess = storageSource.createAccess("save")
    }

    private fun clear() {
        storageAccess?.close()
        storageAccess = null
    }
}