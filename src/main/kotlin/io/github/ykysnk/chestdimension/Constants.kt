@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.ykysnk.chestdimension

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.command.ChestDimCommand
import io.github.ykysnk.chestdimension.config.Configs
import io.github.ykysnk.chestdimension.item.ItemGroups
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.storage.ChestLevelStorage
import io.github.ykysnk.chestdimension.tags.NameSpaces
import io.github.ykysnk.chestdimension.utils.TaskPool
import io.github.ykysnk.chestdimension.world.Network
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectories

object Constants {
    init {
        ServerLifecycleEvents.SERVER_STARTING.register { getServer = it }
        ServerLifecycleEvents.SERVER_STOPPED.register { getServer = null }
    }

    const val MOD_ID: String = "chest-dimension"
    const val MOD_NAME: String = "Chest Dimension"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)

    @JvmStatic
    val ConfigDir: Path by lazy {
        val dir = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        dir.createDirectories()
        dir
    }

    val ForceInitialize: Unit by lazy {
        LOGGER.debug(
            "{} {} {} {} {} {} {} {} {}",
            Configs,
            Blocks,
            Items,
            ItemGroups,
            ChestLevelStorage,
            ChestLevelManager,
            Network,
            ChestDimCommand,
            TaskPool
        )
    }

    private var getServer: MinecraftServer? = null

    val Server: MinecraftServer
        get() = getServer ?: error("Server is not initialized")

    @JvmStatic
    fun id(namespace: String, path: String): ResourceLocation = ResourceLocation(namespace, path)

    @JvmStatic
    fun id(namespace: NameSpaces, path: String): ResourceLocation = id(namespace.id, path)

    @JvmStatic
    fun id(path: String): ResourceLocation = id(NameSpaces.MOD, path)
}

fun id(namespace: String, path: String): ResourceLocation = Constants.id(namespace, path)
fun id(namespace: NameSpaces, path: String): ResourceLocation = Constants.id(namespace, path)
fun id(path: String): ResourceLocation = Constants.id(NameSpaces.MOD, path)