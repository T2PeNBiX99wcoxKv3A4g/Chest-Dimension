@file:Suppress("unused")

package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.event.UtilsEvents
import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.data.UndefinedData
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.level.levelgen.NoiseGeneratorSettings
import io.github.ykysnk.chestdimension.level.storage.ChestLevelStorage
import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import io.github.ykysnk.chestdimension.tags.NameSpaces
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
    private var loaded: UndefinedServerLevel? = null
    private lateinit var chunkProgressListener: ChunkProgressListener
    private val biome by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.GRAVEYARD)
    }
    private val dimensionType by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
            .getHolderOrThrow(DimensionTypes.CHEST_UNDEFINED)
    }
    private val noiseSettings by lazy {
        Constants.Server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
            .getHolderOrThrow(NoiseGeneratorSettings.CHEST_UNDEFINED)
    }

    private val worldKey by lazy { ResourceKey.create(Registries.DIMENSION, NameSpaces.MOD("undefined")) }

    private fun load(server: MinecraftServer, listener: ChunkProgressListener) {
        chunkProgressListener = listener

        val biomeSource = FixedBiomeSource(biome)
        val generator = NoiseBasedChunkGenerator(biomeSource, noiseSettings)
        val levelStem = LevelStem(dimensionType, generator)
        if (!data.created) return
        val seed = data.seed
        val worldOptions = WorldOptions(seed, true, false)
        val level = createServerLevel(server, levelStem, worldOptions)
        server.levels[worldKey] = level
        loaded = level
    }

    fun getOrCreate(server: MinecraftServer): UndefinedServerLevel {
        loaded?.let { return it }

        val worldOptions = WorldOptions.defaultWithRandomSeed()
        val seed = worldOptions.seed()
        val biomeSource = FixedBiomeSource(biome)
        val generator = NoiseBasedChunkGenerator(biomeSource, noiseSettings)
        val levelStem = LevelStem(dimensionType, generator)
        val level = createServerLevel(server, levelStem, worldOptions)
        level.chestServerLevelData.freezeWeather = true
        level.chestServerLevelData.setWeatherParametersForce(0, 10000, true, true)
        server.levels[worldKey] = level
        loaded = level
        data = data.copy(seed, true)
        save()
        return level
    }

    private fun createServerLevel(
        server: MinecraftServer,
        levelStem: LevelStem,
        worldOptions: WorldOptions
    ): UndefinedServerLevel {
        val storage = ChestLevelStorage.access
        val listener = chunkProgressListener
        val isDebugWorld = server.worldData.isDebugWorld
        val obfuscateSeed = BiomeManager.obfuscateSeed(worldOptions.seed())
        val chestServerLevelData = ChestServerLevelData(server.worldData, server.worldData.overworldData())
        chestServerLevelData.freezeWeather = true
        chestServerLevelData.setWeatherParametersForce(0, 10000, true, true)

        ChestServerLevelContext.worldOptions.set(worldOptions)

        return runCatching {
            UndefinedServerLevel(
                server,
                server.executor,
                storage,
                chestServerLevelData,
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

    fun isInsideUndefinedDimension(levelKey: ResourceKey<Level>): Boolean = levelKey == worldKey

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