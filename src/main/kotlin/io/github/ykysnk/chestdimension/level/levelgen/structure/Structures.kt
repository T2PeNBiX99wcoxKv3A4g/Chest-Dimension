package io.github.ykysnk.chestdimension.level.levelgen.structure

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.level.biome.Biomes
import net.minecraft.core.HolderGetter
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure

//import net.minecraft.data.worldgen.Structures as MCStructures

object Structures {
    val DEATH_BODY_CHEST: ResourceKey<Structure> = ResourceKey.create(Registries.STRUCTURE, id("death_body_chest"))
    val SMALL_SHELTER: ResourceKey<Structure> = ResourceKey.create(Registries.STRUCTURE, id("small_shelter"))

    fun bootstrap(context: BootstapContext<Structure>) {
        val biomes: HolderGetter<Biome> = context.lookup(Registries.BIOME)
        val pools: HolderGetter<StructureTemplatePool> = context.lookup(Registries.TEMPLATE_POOL)
        val biomeSet = HolderSet.direct(biomes.getOrThrow(Biomes.CHEST_UNDEFINED_BIOME))
        val settings = Structure.StructureSettings(
            biomeSet,
            emptyMap(),
            GenerationStep.Decoration.SURFACE_STRUCTURES,
            TerrainAdjustment.BURY
        )

        context.register(
            DEATH_BODY_CHEST,
            JigsawStructure(
                settings,
                pools.getOrThrow(TemplatePools.DEATH_BODY_CHEST_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(-2)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        )

        context.register(
            SMALL_SHELTER,
            JigsawStructure(
                settings,
                pools.getOrThrow(TemplatePools.SMALL_SHELTER_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(-1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        )
    }
}