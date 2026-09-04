package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.level.saveddata.ChestSavedData
import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.tags.TagKey
import net.minecraft.util.ProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.concurrent.Executor

open class ChestServerLevel(
    server: MinecraftServer,
    dispatcher: Executor,
    levelStorageAccess: LevelStorageSource.LevelStorageAccess,
    dimension: ResourceKey<Level>,
    levelStem: LevelStem,
    progressListener: ChunkProgressListener,
    isDebug: Boolean,
    biomeZoomSeed: Long,
    private val levelWorldOptions: WorldOptions,
    customSpawners: List<CustomSpawner>,
    private val addDayTime: Long,
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
    constructor(
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
        customSpawners,
        1L,
        randomSequences
    )

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
        1L,
        null
    )

    @Suppress("unused")
    val chestServerLevelData: ChestServerLevelData =
        serverLevelData as? ChestServerLevelData
            ?: throw IllegalArgumentException("serverLevelData are not ChestServerLevelData")

    init {
        dataStorage.computeIfAbsent(
            { loadDimData(it) },
            { ChestSavedData(chestServerLevelData) },
            "chest_dimension_level_data"
        )
    }

    private fun loadDimData(tag: CompoundTag): ChestSavedData {
        chestServerLevelData.load(tag)
        return ChestSavedData(chestServerLevelData)
    }

    override fun toString(): String = "ChestServerLevel[${serverLevelData.levelName}]"

    override fun getSeed(): Long = levelWorldOptions.seed()

    override fun tickTime() {
        if (levelData.gameRules.getBoolean(GameRules.RULE_DAYLIGHT)) dayTime = levelData.dayTime + addDayTime
    }

    override fun findNearestMapStructure(
        structureTag: TagKey<Structure>,
        pos: BlockPos,
        radius: Int,
        skipExistingChunks: Boolean
    ): BlockPos? {
        if (!levelWorldOptions.generateStructures()) {
            return null
        } else {
            val optional = registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(structureTag)
            if (optional.isEmpty) {
                return null
            } else {
                val pair =
                    chunkSource.generator.findNearestMapStructure(this, optional.get(), pos, radius, skipExistingChunks)
                return pair?.getFirst()
            }
        }
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skipSave: Boolean) {
        super.save(progress, flush, skipSave)
        if (!skipSave) return
        UUIDManager.saveNow()
        TeleportManager.saveNow()
    }
}