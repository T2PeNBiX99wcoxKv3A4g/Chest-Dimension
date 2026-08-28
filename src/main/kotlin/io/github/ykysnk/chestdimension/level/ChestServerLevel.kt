package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.level.saveddata.ChestSavedData
import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.concurrent.Executor

class ChestServerLevel(
    server: MinecraftServer,
    dispatcher: Executor,
    levelStorageAccess: LevelStorageSource.LevelStorageAccess,
    dimension: ResourceKey<Level>,
    levelStem: LevelStem,
    progressListener: ChunkProgressListener,
    isDebug: Boolean,
    biomeZoomSeed: Long,
    customSpawners: List<CustomSpawner>,
    randomSequences: RandomSequences?
) : ServerLevel(
    server,
    dispatcher,
    levelStorageAccess,
    ChestServerLevelData(server.worldData, server.worldData.overworldData()),
    dimension,
    levelStem,
    progressListener,
    isDebug,
    biomeZoomSeed,
    customSpawners,
    true,
    randomSequences
) {
    @Suppress("unused")
    private val chestSavedData = dataStorage.computeIfAbsent(
        { loadDimData(it) },
        { ChestSavedData(serverLevelData) },
        "chest_dimension_level_data"
    )

    @Suppress("unused")
    val chestServerLevelData: ChestServerLevelData? = serverLevelData as? ChestServerLevelData

    private fun loadDimData(tag: CompoundTag): ChestSavedData {
        (serverLevelData as? ChestServerLevelData)?.load(tag)
        return ChestSavedData(serverLevelData)
    }

    override fun toString(): String = "ChestServerLevel[${serverLevelData.levelName}]"

    override fun tickTime() {
        if (levelData.gameRules.getBoolean(GameRules.RULE_DAYLIGHT)) dayTime = levelData.dayTime + 1L
    }
}