@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.NameSpaces
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.ChestDimensionBlock
import io.github.ykysnk.chestdimension.event.UtilsEvents
import io.github.ykysnk.chestdimension.extensions.*
import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.chunk.ChestChunkGenerator
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.level.storage.ChestLevelStorage
import io.github.ykysnk.chestdimension.utils.TaskPool
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import java.util.*
import kotlin.math.abs

object ChestLevelManager {
    private val TELEPORT_HORIZONTAL_OFFSETS: List<Vec3i> = (-2..2)
        .flatMap { x -> (-2..2).map { z -> Vec3i(x, 0, z) } }
        .filter { it.x != 0 || it.z != 0 }
        .sortedBy { maxOf(abs(it.x), abs(it.z)) }
    private val TELEPORT_OFFSETS: List<Vec3i> = buildList {
        addAll(TELEPORT_HORIZONTAL_OFFSETS)
        for (i in 1..5) {
            addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.below(i) })
            addAll(TELEPORT_HORIZONTAL_OFFSETS.map { it.above(i) })
            add(Vec3i(0, i, 0))
        }
    }
    private val loaded = mutableMapOf<UUID, LoadedChestLevel>()
    private val levelToUUID = mutableMapOf<ResourceKey<Level>, UUID>()
    private val worldKeyCache = mutableMapOf<UUID, ResourceKey<Level>>()
    private lateinit var chunkProgressListener: ChunkProgressListener
    private val defaultSpawnPos by lazy { BlockPos(0, 1, 0) }
    private val biome by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.PLATFORM)
    }

    private val dimensionType by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.PLATFORM)
    }

    private fun load(server: MinecraftServer, listener: ChunkProgressListener) {
        chunkProgressListener = listener

        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)

        for ((uuidString, data) in UUIDManager.getMap()) {
            val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                Constants.LOGGER.warn("Invalid UUID in levels.yaml: $uuidString (${it.localizedMessage})", it)
                continue
            }
            if (!UUIDManager.isActive(uuid)) continue
            val worldKey = createWorldKey(uuid)
            val seed = data.seed
            val worldOptions = WorldOptions(seed, true, false)
            val level = createServerLevel(server, levelStem, uuid, worldOptions)

            server.levels[worldKey] = level
            loaded[uuid] = LoadedChestLevel(uuid, level)
            levelToUUID[worldKey] = uuid
        }
    }

    fun getOrCreate(server: MinecraftServer, uuid: UUID): ChestServerLevel {
        loaded[uuid]?.let {
            return it.level
        }

        val worldKey = createWorldKey(uuid)
        val worldOptions = WorldOptions.defaultWithRandomSeed()
        val seed = worldOptions.seed()
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)
        val level = createServerLevel(server, levelStem, uuid, worldOptions)
        createStartPlatform(level)

        server.levels[worldKey] = level
        loaded[uuid] = LoadedChestLevel(uuid, level)
        levelToUUID[worldKey] = uuid
        UUIDManager.add(uuid, seed)
        UUIDManager.save()
        return level
    }

    private fun createStartPlatform(level: ChestServerLevel) {
        val platformY = 0
        val minX = -20
        val maxX = 20
        val minZ = -20
        val maxZ = 20

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                level.setBlock(BlockPos(x, platformY, z), Blocks.CHEST_PLATFORM.defaultBlockState(), 3)
            }
        }

        for (x in minX..maxX) {
            level.setBlock(BlockPos(x, platformY + 1, minZ), Blocks.CHEST_PLATFORM_WALL.defaultBlockState(), 3)
            level.setBlock(BlockPos(x, platformY + 1, maxZ), Blocks.CHEST_PLATFORM_WALL.defaultBlockState(), 3)
        }

        for (z in minZ..maxZ) {
            level.setBlock(BlockPos(minX, platformY + 1, z), Blocks.CHEST_PLATFORM_WALL.defaultBlockState(), 3)
            level.setBlock(BlockPos(maxX, platformY + 1, z), Blocks.CHEST_PLATFORM_WALL.defaultBlockState(), 3)
        }

        for (x in minX..maxX) {
            updateWall(level, BlockPos(x, platformY + 1, minZ))
            updateWall(level, BlockPos(x, platformY + 1, maxZ))
        }

        for (z in minZ..maxZ) {
            updateWall(level, BlockPos(minX, platformY + 1, z))
            updateWall(level, BlockPos(maxX, platformY + 1, z))
        }

        level.setBlock(BlockPos(0, platformY + 1, 0), Blocks.CHEST_PLATFORM_ENTER_PLATE.defaultBlockState(), 3)
        level.setBlock(
            BlockPos(maxX - 2, platformY + 1, minZ + 2),
            Blocks.TELEPORT_PRESSURE_PLATE.defaultBlockState(),
            3
        )
        level.setBlock(
            BlockPos(minX + 2, platformY + 1, minZ + 2),
            Blocks.WEATHER_TIME_CONTROLLER.defaultBlockState(),
            3
        )
    }

    private fun updateWall(level: ChestServerLevel, pos: BlockPos) {
        val state = level.getBlockState(pos)

        if (state.block is WallBlock) {
            val updated = Block.updateFromNeighbourShapes(state, level, pos)
            level.setBlock(pos, updated, 3)
        }
    }

    operator fun get(uuid: UUID): ChestServerLevel? = loaded[uuid]?.level

    private fun handleEntityTeleportToExit(entity: Entity, teleportTo: ServerLevel, teleportPos: BlockPos): Boolean {
        val overworld = Constants.Server.overworld()
        entity.resetFallDistance()
        (teleportTo.getBlockState(teleportPos).block as? ChestDimensionBlock)?.let { _ ->
            entity.findSafeLocation(teleportTo, teleportPos, TELEPORT_OFFSETS)?.let { safePos ->
                if (entity.teleportToLevel(teleportTo, safePos)) return true
            }

            entity.findNonCollidingAbovePosition(teleportTo, teleportPos)?.let { topPos ->
                if (entity.teleportToLevel(teleportTo, topPos)) return true
            }

            if (!entity.teleportToSafeLocation(teleportTo, teleportPos))
                if (entity.teleportToSpawnLocation(overworld)) return true
        }

        return entity.teleportToSpawnLocation(overworld)
    }

    fun teleportEntityToExit(level: Level, entity: Entity): Boolean = teleportEntitiesToExit(level, listOf(entity))

    fun teleportEntitiesToExit(level: Level, entities: List<Entity>): Boolean {
        if (level.isClientSide || level !is ServerLevel || !isInsideChestDimension(level)) return false
        val overworld = Constants.Server.overworld()
        val uuid = findUUIDByLevel(level)
        uuid?.let {
            UUIDManager.save()
            val chestDim = UUIDManager.getExitChestDimension(it)
            val chestPos = UUIDManager.getExitChestPosition(it)
            entities.forEach { entity ->
                when {
                    chestDim != null && chestPos != null -> handleEntityTeleportToExit(
                        entity,
                        chestDim,
                        chestPos
                    )

                    else -> {
                        entity.resetFallDistance()
                        entity.teleportToSpawnLocation(overworld)
                    }
                }
            }

            return true
        }
        return false
    }

    private fun handleEntityTeleportToEnter(
        entity: Entity,
        teleportTo: ServerLevel,
        teleportPosList: List<BlockPos>
    ): Boolean {
        entity.resetFallDistance()

        if (teleportPosList.isNotEmpty()) {
            for (pos in teleportPosList.shuffled()) {
                entity.findNonCollidingPosition(teleportTo, pos)?.let { findPos ->
                    if (entity.teleportToLevel(teleportTo, findPos, true)) return true
                }
            }
        }

        entity.findNonCollidingPosition(teleportTo, defaultSpawnPos)?.let { findPos ->
            if (entity.teleportToLevel(teleportTo, findPos, true)) return true
        }

        entity.findSafeLocation(teleportTo, defaultSpawnPos, TELEPORT_OFFSETS)?.let { findPos ->
            if (entity.teleportToLevel(teleportTo, findPos, true)) return true
        }

        return entity.teleportToSafeLocation(teleportTo, defaultSpawnPos, true)
    }

    fun teleportEntityToEnter(level: Level, entity: Entity): Boolean = teleportEntitiesToEnter(level, listOf(entity))

    fun teleportEntitiesToEnter(level: Level, entities: List<Entity>): Boolean {
        if (level.isClientSide || level !is ServerLevel || !isInsideChestDimension(level)) return false
        (level as? ChestServerLevel)?.apply {
            val spawnPosList = chestServerLevelData.getSpawnPosList()
            entities.forEach { entity ->
                handleEntityTeleportToEnter(entity, this, spawnPosList)
            }
            return true
        }
        return false
    }

    fun addSpawnPos(level: Level, pos: BlockPos) {
        if (level.isClientSide) return
        (level as? ChestServerLevel)?.apply {
            chestServerLevelData.addSpawnPos(pos)
        }
    }

    fun removeSpawnPos(level: Level, pos: BlockPos) {
        if (level.isClientSide) return
        (level as? ChestServerLevel)?.apply {
            chestServerLevelData.removeSpawnPos(pos)
        }
    }

    fun setInactive(uuid: UUID) {
        if (!UUIDManager.isExist(uuid)) return
        UUIDManager.setInactive(uuid)
        UUIDManager.clearChestData(uuid)
    }

    fun setActive(uuid: UUID) {
        if (!UUIDManager.isExist(uuid)) return
        UUIDManager.setActive(uuid)
        if (loaded.containsKey(uuid)) return
        val data = UUIDManager[uuid] ?: return
        val server = Constants.Server
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)
        val worldKey = createWorldKey(uuid)
        val seed = data.seed
        val worldOptions = WorldOptions(seed, true, false)
        val level = createServerLevel(server, levelStem, uuid, worldOptions)

        server.levels[worldKey] = level
        loaded[uuid] = LoadedChestLevel(uuid, level)
        levelToUUID[worldKey] = uuid
        UUIDManager.save()
    }

    private fun createServerLevel(
        server: MinecraftServer,
        levelStem: LevelStem,
        uuid: UUID,
        worldOptions: WorldOptions
    ): ChestServerLevel {
        val storage = ChestLevelStorage.access
        val listener = chunkProgressListener
        val isDebugWorld = server.worldData.isDebugWorld
        val worldKey = createWorldKey(uuid)
        val obfuscateSeed = BiomeManager.obfuscateSeed(worldOptions.seed())

        ChestServerLevelContext.worldOptions.set(worldOptions)

        return runCatching {
            ChestServerLevel(
                server,
                server.executor,
                storage,
                worldKey,
                levelStem,
                listener,
                isDebugWorld,
                obfuscateSeed
            )
        }.also {
            ChestServerLevelContext.worldOptions.remove()
        }.getOrThrow()
    }

    fun findUUIDByLevel(level: Level): UUID? = levelToUUID[level.dimension()]

    fun isInsideChestDimension(level: Level): Boolean = isInsideChestDimension(level.dimension())

    fun isInsideChestDimension(levelKey: ResourceKey<Level>): Boolean =
        levelKey.location().toString().startsWith("${NameSpaces.MOD}:chest/")

    private fun createWorldKey(uuid: UUID): ResourceKey<Level> {
        worldKeyCache[uuid]?.let { return it }
        val key = ResourceKey.create(Registries.DIMENSION, NameSpaces.MOD("chest/$uuid/platform"))
        return worldKeyCache.putAndGet(uuid, key)
    }

    private fun checkLevels() {
        val badData = hashSetOf<UUID>()
        val reallyBadData = hashSetOf<String>()
        UUIDManager.getMap().forEach { (uuidString, _) ->
            val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                reallyBadData.add(uuidString)
                Constants.LOGGER.warn("Invalid UUID in levels.yaml: $uuidString (${it.localizedMessage})", it)
                return@forEach
            }
            if (!UUIDManager.isActive(uuid)) return@forEach
            val level = UUIDManager.getExitChestDimension(uuid)
            if (level == null) {
                badData.add(uuid)
                Constants.LOGGER.warn("Invalid dimension in levels.yaml: $uuidString")
                return@forEach
            }
            val pos = UUIDManager.getExitChestPosition(uuid)
            if (pos == null) {
                badData.add(uuid)
                Constants.LOGGER.warn("Invalid position in levels.yaml: $uuidString")
                return@forEach
            }
            if (level.getBlockState(pos).block is ChestDimensionBlock) return@forEach
            badData.add(uuid)
        }
        badData.forEach { setInactive(it) }
        reallyBadData.forEach { UUIDManager.remove(it) }
        UUIDManager.save()
    }

    private fun clear() {
        loaded.clear()
        levelToUUID.clear()
    }

    init {
        UtilsEvents.AFTER_CREATE_LEVEL.register(::load)
        ServerLifecycleEvents.SERVER_STARTING.register {
            TaskPool.run(::checkLevels)
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            clear()
        }
    }
}