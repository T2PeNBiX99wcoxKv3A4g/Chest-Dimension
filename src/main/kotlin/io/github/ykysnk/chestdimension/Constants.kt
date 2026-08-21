package io.github.ykysnk.chestdimension

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.item.ItemGroups
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.ChestLevelStorage
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectories

@Suppress("unused")
object Constants {
    init {
        ServerLifecycleEvents.SERVER_STARTING.register { getServer = it }
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
        LOGGER.debug("{} {} {} {} {}", Blocks, Items, ItemGroups, ChestLevelStorage, ChestLevelManager)
    }

    private var getServer: MinecraftServer? = null

    val Server: MinecraftServer
        get() = getServer ?: error("Server is not initialized")

    @JvmStatic
    fun id(path: String) = ResourceLocation(MOD_ID, path)

    fun Entity.teleportToLevel(level: ServerLevel) = teleportTo(level, 0.5, 100.0, 0.5, setOf(), yRot, xRot)
    fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3) =
        teleportTo(level, pos.x, pos.y, pos.z, setOf(), yRot, xRot)

    fun <T> MutableList<T>.funcAndListAdd(func: () -> T) : T {
        val item = func()
        add(item)
        return item
    }
}