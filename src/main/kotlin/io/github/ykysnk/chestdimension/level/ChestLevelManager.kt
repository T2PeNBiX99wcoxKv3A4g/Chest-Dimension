package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.DerivedLevelData
import java.util.*

object ChestLevelManager {
    init {
        ServerLifecycleEvents.SERVER_STOPPING.register {
            clear()
        }
    }

    private val loaded = mutableMapOf<UUID, LoadedChestWorld>()
    private var chunkProgressListener: ChunkProgressListener? = null

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

        for ((uuid, data) in UUIDManager.get()) {
            val uuid2 = runCatching { UUID.fromString(uuid) }.getOrElse {
                Constants.LOGGER.warn("Invalid UUID in levels.yaml: $uuid (${it.localizedMessage})", it)
                continue
            }
            val worldKey = createWorldKey(uuid2)
            val obfuscateSeed = BiomeManager.obfuscateSeed(data.seed)
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
        UUIDManager.add(uuid, LevelData(seed))
        UUIDManager.save()
        return level
    }

    private fun createStartPlatform(level: ServerLevel) {
        val platformY = 0
        val minX = -8
        val maxX = 7
        val minZ = -8
        val maxZ = 7

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
    }

    private fun updateWall(level: ServerLevel, pos: BlockPos) {
        val state = level.getBlockState(pos)

        if (state.block is WallBlock) {
            val updated = Block.updateFromNeighbourShapes(state, level, pos)
            level.setBlock(pos, updated, 3)
        }
    }

    operator fun get(uuid: UUID): ServerLevel? = loaded[uuid]?.level

    fun createWorldKey(uuid: UUID): ResourceKey<Level> =
        ResourceKey.create(Registries.DIMENSION, Constants.id("chest/$uuid"))

    fun clear() {
        loaded.clear()
    }
}