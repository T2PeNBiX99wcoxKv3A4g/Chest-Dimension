package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.ChestDimensionBlock
import io.github.ykysnk.chestdimension.extensions.*
import io.github.ykysnk.chestdimension.level.storage.ChestLevelStorage
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
    private val loaded = mutableMapOf<UUID, LoadedChestWorld>()
    private val levelToUUID = mutableMapOf<ResourceKey<Level>, UUID>()
    private lateinit var chunkProgressListener: ChunkProgressListener
    private val defaultSpawnPos by lazy { BlockPos(0, 1, 0) }
    private val biome by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.CHEST_BIOME)
    }

    private val dimensionType by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST)
    }

    fun load(server: MinecraftServer, listener: ChunkProgressListener) {
        chunkProgressListener = listener

        val storage = ChestLevelStorage.access
        val isDebugWorld = server.worldData.isDebugWorld
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)

        for ((uuid, data) in UUIDManager.getMap()) {
            val uuid2 = runCatching { UUID.fromString(uuid) }.getOrElse {
                Constants.LOGGER.warn("Invalid UUID in levels.yaml: $uuid (${it.localizedMessage})", it)
                continue
            }
            if (!UUIDManager.isActive(uuid2)) continue
            val worldKey = createWorldKey(uuid2)
            val seed = data.seed
            val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
            val level = ChestServerLevel(
                server,
                server.executor,
                storage,
                worldKey,
                levelStem,
                listener,
                isDebugWorld,
                obfuscateSeed,
                emptyList(),
                null
            )

            server.levels[worldKey] = level
            loaded[uuid2] = LoadedChestWorld(uuid2, level)
            levelToUUID[worldKey] = uuid2
        }
    }

    fun getOrCreate(server: MinecraftServer, uuid: UUID): ChestServerLevel {
        loaded[uuid]?.let {
            return it.level
        }

        val storage = ChestLevelStorage.access
        val worldKey = createWorldKey(uuid)
        val listener = chunkProgressListener
        val isDebugWorld = server.worldData.isDebugWorld
        val worldOptions = WorldOptions.defaultWithRandomSeed()
        val seed = worldOptions.seed()
        val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)
        val level = ChestServerLevel(
            server,
            server.executor,
            storage,
            worldKey,
            levelStem,
            listener,
            isDebugWorld,
            obfuscateSeed,
            emptyList(),
            null
        )
        createStartPlatform(level)

        server.levels[worldKey] = level
        loaded[uuid] = LoadedChestWorld(uuid, level)
        levelToUUID[worldKey] = uuid
        UUIDManager.add(uuid, seed)
        UUIDManager.addSpawnPos(uuid, BlockPos(0, 1, 0))
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
            BlockPos(maxX - 1, platformY + 1, minZ + 1),
            Blocks.TELEPORT_PRESSURE_PLATE.defaultBlockState(),
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
        val uuid = findUUIDByLevel(level)
        uuid?.let {
            val spawnPosList = UUIDManager.getSpawnPosList(it)
            spawnPosList?.let { list ->
                entities.forEach { entity ->
                    handleEntityTeleportToEnter(entity, level, list)
                }
            }
            return true
        }
        return false
    }

    fun addSpawnPos(level: Level, pos: BlockPos) {
        if (level.isClientSide) return
        val uuid = findUUIDByLevel(level)
        uuid?.let { UUIDManager.addSpawnPos(it, pos) }
    }

    fun removeSpawnPos(level: Level, pos: BlockPos) {
        if (level.isClientSide) return
        val uuid = findUUIDByLevel(level)
        uuid?.let { UUIDManager.removeSpawnPos(it, pos) }
    }

    fun setInactive(uuid: UUID) {
        UUIDManager.setInactive(uuid)
        UUIDManager.clearChestData(uuid)
    }

    fun setActive(uuid: UUID) {
        UUIDManager.setActive(uuid)
        if (loaded.containsKey(uuid)) return
        val data = UUIDManager[uuid] ?: return
        val server = Constants.Server
        val storage = ChestLevelStorage.access
        val listener = chunkProgressListener
        val isDebugWorld = server.worldData.isDebugWorld
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val levelStem = LevelStem(dimensionType, generator)
        val worldKey = createWorldKey(uuid)
        val seed = data.seed
        val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
        val level = ChestServerLevel(
            server,
            server.executor,
            storage,
            worldKey,
            levelStem,
            listener,
            isDebugWorld,
            obfuscateSeed,
            emptyList(),
            null
        )

        server.levels[worldKey] = level
        loaded[uuid] = LoadedChestWorld(uuid, level)
        levelToUUID[worldKey] = uuid
        UUIDManager.save()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun findUUIDByLevel(level: Level): UUID? = levelToUUID[level.dimension()]

    @Suppress("MemberVisibilityCanBePrivate")
    fun isInsideChestDimension(level: Level): Boolean =
        level.dimension().location().toString().startsWith("${Constants.MOD_ID}:chest/")

    private fun createWorldKey(uuid: UUID): ResourceKey<Level> =
        ResourceKey.create(Registries.DIMENSION, Constants.id("chest/$uuid"))

    private fun clear() {
        loaded.clear()
        levelToUUID.clear()
    }

    init {
        ServerLifecycleEvents.SERVER_STOPPING.register {
            clear()
        }
    }
}