package io.github.ykysnk.chestdimension.level

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class ChestChunkGenerator(biomeSource: BiomeSource) : ChunkGenerator(biomeSource) {
    companion object {
        val CODEC: Codec<ChestChunkGenerator> = BiomeSource.CODEC.xmap(::ChestChunkGenerator, ChestChunkGenerator::biomeSource)
    }

    override fun codec(): Codec<ChestChunkGenerator> {
        return CODEC
    }

    override fun applyCarvers(
        level: WorldGenRegion,
        seed: Long,
        random: RandomState,
        biomeManager: BiomeManager,
        structureManager: StructureManager,
        chunk: ChunkAccess,
        step: GenerationStep.Carving
    ) {
    }

    override fun buildSurface(
        level: WorldGenRegion,
        structureManager: StructureManager,
        random: RandomState,
        chunk: ChunkAccess
    ) {
    }

    override fun spawnOriginalMobs(level: WorldGenRegion) {
    }

    override fun getMinY(): Int {
        return -128
    }

    override fun getBaseHeight(
        x: Int,
        z: Int,
        type: Heightmap.Types,
        level: LevelHeightAccessor,
        random: RandomState
    ): Int {
        return level.minBuildHeight
    }

    override fun getBaseColumn(
        x: Int,
        z: Int,
        height: LevelHeightAccessor,
        random: RandomState
    ): NoiseColumn = NoiseColumn(height.minBuildHeight, Array(height.height) { Blocks.AIR.defaultBlockState() })

    override fun addDebugScreenInfo(
        info: List<String>,
        random: RandomState,
        pos: BlockPos
    ) {
    }

    override fun getGenDepth(): Int {
        return 1280
    }

    override fun getSeaLevel(): Int {
        return -63
    }

    override fun getSpawnHeight(level: LevelHeightAccessor): Int = 0

    override fun fillFromNoise(
        executor: Executor,
        blender: Blender,
        random: RandomState,
        structureManager: StructureManager,
        chunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> = CompletableFuture.completedFuture(chunk)
}