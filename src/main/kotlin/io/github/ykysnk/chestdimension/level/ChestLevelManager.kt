package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.ChestDimensionBlock
import io.github.ykysnk.chestdimension.extensions.*
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
import net.minecraft.world.level.storage.DerivedLevelData
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
    private var chunkProgressListener: ChunkProgressListener? = null
    private val defaultSpawnPos by lazy { BlockPos(0, 1, 0) }

    fun load(server: MinecraftServer, listener: ChunkProgressListener) {
        chunkProgressListener = listener

        val storage = ChestLevelStorage.access
        val levelData = DerivedLevelData(server.worldData, server.worldData.overworldData())
        val isDebugWorld = server.worldData.isDebugWorld
        val biome = server.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.CHEST_BIOME)
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val dimensionType = server.registryAccess()
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST)
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
            val level = ServerLevel(
                server,
                server.executor,
                storage,
                levelData,
                worldKey,
                levelStem,
                listener,
                isDebugWorld,
                obfuscateSeed,
                emptyList(),
                true,
                null
            )

            server.levels[worldKey] = level
            loaded[uuid2] = LoadedChestWorld(uuid2, level)
            levelToUUID[worldKey] = uuid2
        }
    }

    fun getOrCreate(server: MinecraftServer, uuid: UUID): ServerLevel {
        loaded[uuid]?.let {
            return it.level
        }

        val storage = ChestLevelStorage.access
        val worldKey = createWorldKey(uuid)
        val levelData = DerivedLevelData(server.worldData, server.worldData.overworldData())
        val listener = chunkProgressListener ?: server.progressListenerFactory.create(11)
        val isDebugWorld = server.worldData.isDebugWorld
        val worldOptions = WorldOptions.defaultWithRandomSeed()
        val seed = worldOptions.seed()
        val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
        val biome = server.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.CHEST_BIOME)
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val dimensionType = server.registryAccess()
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST)
        val levelStem = LevelStem(dimensionType, generator)
        val level = ServerLevel(
            server,
            server.executor,
            storage,
            levelData,
            worldKey,
            levelStem,
            listener,
            isDebugWorld,
            obfuscateSeed,
            emptyList(),
            true,
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

    private fun createStartPlatform(level: ServerLevel) {
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

    private fun updateWall(level: ServerLevel, pos: BlockPos) {
        val state = level.getBlockState(pos)

        if (state.block is WallBlock) {
            val updated = Block.updateFromNeighbourShapes(state, level, pos)
            level.setBlock(pos, updated, 3)
        }
    }

    operator fun get(uuid: UUID): ServerLevel? = loaded[uuid]?.level

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

    fun teleportEntityToExit(level: Level, entity: Entity): Boolean {
        if (level.isClientSide || level !is ServerLevel || !isInsideChestDimension(level)) return false
        val overworld = Constants.Server.overworld()
        val uuid = findUUIDByLevel(level)
        uuid?.let {
            UUIDManager.save()
            val chestDim = UUIDManager.getExitChestDimension(it)
            val chestPos = UUIDManager.getExitChestPosition(it)
            when {
                chestDim != null && chestPos != null -> if (handleEntityTeleportToExit(
                        entity,
                        chestDim,
                        chestPos
                    )
                ) return true

                else -> {
                    entity.resetFallDistance()
                    if (entity.teleportToSpawnLocation(overworld)) return true
                }
            }
        }
        return false
    }

    fun teleportEntitiesToExit(level: Level, entities: List<Entity>): Boolean {
        if (level.isClientSide || level !is ServerLevel || !isInsideChestDimension(level)) return false
        val overworld = Constants.Server.overworld()
        val uuid = findUUIDByLevel(level)
        uuid?.let {
            UUIDManager.save()
            val chestDim = UUIDManager.getExitChestDimension(it)
            val chestPos = UUIDManager.getExitChestPosition(it)
            when {
                chestDim != null && chestPos != null -> {
                    entities.forEach { entity ->
                        handleEntityTeleportToExit(entity, chestDim, chestPos)
                    }
                }

                else -> {
                    entities.forEach { entity ->
                        entity.resetFallDistance()
                        entity.teleportToSpawnLocation(overworld)
                    }
                }
            }
            return true
        }
        return false
    }

    fun teleportEntityToEnter(level: Level, entity: Entity): Boolean {
        TODO("Not yet implemented")
    }

    fun teleportEntitiesToEnter(level: Level, entities: List<Entity>): Boolean {
        TODO("Not yet implemented")
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
        val levelData = DerivedLevelData(server.worldData, server.worldData.overworldData())
        val listener = chunkProgressListener ?: server.progressListenerFactory.create(11)
        val isDebugWorld = server.worldData.isDebugWorld
        val biome = server.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.CHEST_BIOME)
        val biomeSource = FixedBiomeSource(biome)
        val generator = ChestChunkGenerator(biomeSource)
        val dimensionType = server.registryAccess()
            .registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST)
        val levelStem = LevelStem(dimensionType, generator)
        val worldKey = createWorldKey(uuid)
        val seed = data.seed
        val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
        val level = ServerLevel(
            server,
            server.executor,
            storage,
            levelData,
            worldKey,
            levelStem,
            listener,
            isDebugWorld,
            obfuscateSeed,
            emptyList(),
            true,
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