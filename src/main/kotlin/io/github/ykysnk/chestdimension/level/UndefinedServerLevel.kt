package io.github.ykysnk.chestdimension.level

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.util.ProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.concurrent.Executor
import kotlin.random.Random

class UndefinedServerLevel(
    server: MinecraftServer,
    dispatcher: Executor,
    levelStorageAccess: LevelStorageSource.LevelStorageAccess,
    dimension: ResourceKey<Level>,
    levelStem: LevelStem,
    progressListener: ChunkProgressListener,
    isDebug: Boolean,
    biomeZoomSeed: Long,
    levelWorldOptions: WorldOptions,
    customSpawners: List<CustomSpawner>,
    randomSequences: RandomSequences?
) : ChestServerLevel(
    server,
    dispatcher,
    levelStorageAccess,
    dimension,
    levelStem,
    progressListener,
    isDebug,
    biomeZoomSeed,
    levelWorldOptions,
    customSpawners,
    randomSequences
) {
    constructor(
        server: MinecraftServer,
        dispatcher: Executor,
        levelStorageAccess: LevelStorageSource.LevelStorageAccess,
        dimension: ResourceKey<Level>,
        levelStem: LevelStem,
        progressListener: ChunkProgressListener,
        isDebug: Boolean,
        biomeZoomSeed: Long,
        levelWorldOptions: WorldOptions
    ) : this(
        server,
        dispatcher,
        levelStorageAccess,
        dimension,
        levelStem,
        progressListener,
        isDebug,
        biomeZoomSeed,
        levelWorldOptions,
        emptyList(),
        null
    )

    override fun toString(): String = "UndefinedServerLevel[${serverLevelData.levelName}]"

    override fun tickTime() {
        if (!levelData.gameRules.getBoolean(GameRules.RULE_DAYLIGHT)) return
        dayTime = Random(seed).nextLong(0L, 240000000000L)
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skipSave: Boolean) {
        super.save(progress, flush, skipSave)
        if (!skipSave) return
        UndefinedLevelManager.saveNow()
    }
}