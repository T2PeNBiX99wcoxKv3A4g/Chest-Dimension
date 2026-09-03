@file:Suppress("unused")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.event.UtilsEvents
import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.data.UndefinedData
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.level.levelgen.NoiseGeneratorSettings
import io.github.ykysnk.chestdimension.level.storage.ChestLevelStorage
import io.github.ykysnk.chestdimension.utils.AbstractManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.WorldOptions
import java.io.File

object UndefinedLevelManager : AbstractManager<UndefinedData>("undefined.dat", UndefinedData(-1L)) {
    private var loaded: ChestServerLevel? = null
    private lateinit var chunkProgressListener: ChunkProgressListener
    private val biome by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.CHEST_UNDEFINED_BIOME)
    }
    private val dimensionType by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST_UNDEFINED)
    }

    private fun load(server: MinecraftServer, listener: ChunkProgressListener) {
        chunkProgressListener = listener

        val storage = ChestLevelStorage.access
        val isDebugWorld = server.worldData.isDebugWorld
        val biomeSource = FixedBiomeSource(biome)
        val noiseSettings = server.registries().compositeAccess()
            .lookupOrThrow(Registries.NOISE_SETTINGS)
            .getOrThrow(NoiseGeneratorSettings.CHEST_UNDEFINED)
        val generator = NoiseBasedChunkGenerator(biomeSource, noiseSettings)
        val levelStem = LevelStem(dimensionType, generator)
        if (!data.created) return
        val worldKey = createWorldKey()
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
        loaded = level
    }

    fun getOrCreate(server: MinecraftServer): ChestServerLevel {
        loaded?.let { return it }

        val storage = ChestLevelStorage.access
        val worldKey = createWorldKey()
        val listener = chunkProgressListener
        val isDebugWorld = server.worldData.isDebugWorld
        val worldOptions = WorldOptions.defaultWithRandomSeed()
        val seed = worldOptions.seed()
        val obfuscateSeed = BiomeManager.obfuscateSeed(seed)
        val biomeSource = FixedBiomeSource(biome)
        val noiseSettings = server.registries().compositeAccess()
            .lookupOrThrow(Registries.NOISE_SETTINGS)
            .getOrThrow(NoiseGeneratorSettings.CHEST_UNDEFINED)
        val generator = NoiseBasedChunkGenerator(biomeSource, noiseSettings)
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
        server.levels[worldKey] = level
        loaded = level
        return level
    }

    private fun createWorldKey(): ResourceKey<Level> =
        ResourceKey.create(Registries.DIMENSION, id("undefined"))

    override fun loadData(): UndefinedData {
        val tag = NbtIo.readCompressed(File(dataPath.toUri()))
        return UndefinedData.load(tag)
    }

    override fun saveNow(saveData: UndefinedData) {
        NbtIo.writeCompressed(saveData.save(), File(dataPath.toUri()))
    }

    private fun clear() {
        loaded = null
    }

    init {
        UtilsEvents.AFTER_CREATE_LEVEL.register(::load)
        ServerLifecycleEvents.SERVER_STOPPING.register {
            clear()
        }
    }
}