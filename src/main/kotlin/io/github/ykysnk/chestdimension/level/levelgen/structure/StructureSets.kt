@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level.levelgen.structure

import io.github.ykysnk.chestdimension.id
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType

object StructureSets {
    val DEATH_BODY_CHEST_SET: ResourceKey<StructureSet> =
        ResourceKey.create(Registries.STRUCTURE_SET, id("death_body_chest"))

    val SMALL_SHELTER_SET: ResourceKey<StructureSet> =
        ResourceKey.create(Registries.STRUCTURE_SET, id("small_shelter"))

    fun bootstrap(context: BootstapContext<StructureSet>) {
        val structures: HolderGetter<Structure> = context.lookup(Registries.STRUCTURE)

        context.register(
            DEATH_BODY_CHEST_SET,
            StructureSet(
                structures.getOrThrow(Structures.DEATH_BODY_CHEST),
                RandomSpreadStructurePlacement(
                    20,
                    10,
                    RandomSpreadType.LINEAR,
                    1709669654
                )
            )
        )

        context.register(
            SMALL_SHELTER_SET,
            StructureSet(
                structures.getOrThrow(Structures.SMALL_SHELTER),
                RandomSpreadStructurePlacement(
                    50,
                    40,
                    RandomSpreadType.LINEAR,
                    2067080282
                )
            )
        )
    }
}