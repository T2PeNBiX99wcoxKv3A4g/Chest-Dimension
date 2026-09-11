@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level.levelgen.structure

import com.mojang.datafixers.util.Pair
import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList


object TemplatePools {
    val EMPTY_PROCESSOR_LIST: ResourceKey<StructureProcessorList> =
        ResourceKey.create(Registries.PROCESSOR_LIST, NameSpaces.MINECRAFT("empty"))
    val EMPTY_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MINECRAFT("empty"))
    val DEATH_BODY_CHEST_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MOD("death_body_chest"))
    val SMALL_SHELTER_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MOD("small_shelter"))

    val BASEMENT_ENTRANCE_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MOD("small_shelter/basement/entrance"))

    val BASEMENT_HALLWAY_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MOD("small_shelter/basement/hallway"))

    val BASEMENT_ROOM_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, NameSpaces.MOD("small_shelter/basement/room"))

    fun bootstrap(context: BootstapContext<StructureTemplatePool>) {
        context.apply {
            val processors = lookup(Registries.PROCESSOR_LIST)
            val emptyProcessors = processors.getOrThrow(EMPTY_PROCESSOR_LIST)
            val fallback = lookup(Registries.TEMPLATE_POOL).getOrThrow(EMPTY_POOL)
            val deathBodyChestElements = listOf(
                Pair.of(
                    SinglePoolElement.single(NameSpaces.MOD("death_body_chest").toString(), emptyProcessors)
                        .apply(StructureTemplatePool.Projection.TERRAIN_MATCHING) as StructurePoolElement, 1
                )
            )
            val smallShelterElements = listOf(
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/main").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
                )
            )
            val basementEntranceElements = listOf(
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/entrance/stairwell").toString(), emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/entrance/storage").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 3
                )
            )
            val basementHallwayElements = listOf(
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/corner").toString(), emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 3
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/cross").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/dead_end").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/junction").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/room_enter").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/hallway/stairwell").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                )
            )
            val basementRoomElements = listOf(
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/death_body").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/empty").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 3
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/enchanting").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/farm").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/kitchen").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/smeltery").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 2
                ),
                Pair.of(
                    SinglePoolElement.single(
                        NameSpaces.MOD("small_shelter/basement/room/storage").toString(),
                        emptyProcessors
                    ).apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
                )
            )

            register(DEATH_BODY_CHEST_POOL, StructureTemplatePool(fallback, deathBodyChestElements))
            register(SMALL_SHELTER_POOL, StructureTemplatePool(fallback, smallShelterElements))
            register(BASEMENT_ENTRANCE_POOL, StructureTemplatePool(fallback, basementEntranceElements))
            register(BASEMENT_HALLWAY_POOL, StructureTemplatePool(fallback, basementHallwayElements))
            register(BASEMENT_ROOM_POOL, StructureTemplatePool(fallback, basementRoomElements))
        }
    }
}