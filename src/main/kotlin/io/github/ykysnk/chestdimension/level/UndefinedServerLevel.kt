package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.util.ProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.concurrent.Executor
import kotlin.random.Random

class UndefinedServerLevel(
    server: MinecraftServer,
    dispatcher: Executor,
    levelStorageAccess: LevelStorageSource.LevelStorageAccess,
    chestServerLevelData: ChestServerLevelData,
    dimension: ResourceKey<Level>,
    levelStem: LevelStem,
    progressListener: ChunkProgressListener,
    isDebug: Boolean,
    biomeZoomSeed: Long,
    customSpawners: List<CustomSpawner>,
    randomSequences: RandomSequences?
) : ChestServerLevel(
    server,
    dispatcher,
    levelStorageAccess,
    chestServerLevelData,
    dimension,
    levelStem,
    progressListener,
    isDebug,
    biomeZoomSeed,
    customSpawners,
    1L,
    randomSequences
) {
    constructor(
        server: MinecraftServer,
        dispatcher: Executor,
        levelStorageAccess: LevelStorageSource.LevelStorageAccess,
        chestServerLevelData: ChestServerLevelData,
        dimension: ResourceKey<Level>,
        levelStem: LevelStem,
        progressListener: ChunkProgressListener,
        isDebug: Boolean,
        biomeZoomSeed: Long
    ) : this(
        server,
        dispatcher,
        levelStorageAccess,
        chestServerLevelData,
        dimension,
        levelStem,
        progressListener,
        isDebug,
        biomeZoomSeed,
        emptyList(),
        null
    )

    private val getRandom by lazy { Random(seed) }

    override fun toString(): String = "UndefinedServerLevel[${serverLevelData.levelName}]"

    override fun tickTime() {
        if (!levelData.gameRules.getBoolean(GameRules.RULE_DAYLIGHT)) return
        dayTime = getRandom.nextLong(0L, 240000000000L)
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skipSave: Boolean) {
        super.save(progress, flush, skipSave)
        if (!skipSave) return
        UndefinedLevelManager.saveNow()
    }

    override fun isRaining(): Boolean = true

    override fun advanceWeatherCycle() {
        if (oRainLevel != 1f) {
            server.playerList.broadcastAll(
                ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F),
                dimension()
            )
            server.playerList.broadcastAll(
                ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1f),
                dimension()
            )
            oRainLevel = 1f
        }

        if (oThunderLevel != 1f) {
            server.playerList.broadcastAll(
                ClientboundGameEventPacket(
                    ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
                    1f
                ), dimension()
            )
            oThunderLevel = 1f
        }
    }
}