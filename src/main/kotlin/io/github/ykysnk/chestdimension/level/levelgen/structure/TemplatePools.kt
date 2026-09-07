@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.level.levelgen.structure

import com.mojang.datafixers.util.Pair
import io.github.ykysnk.chestdimension.id
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList


object TemplatePools {
    val EMPTY_PROCESSOR_LIST: ResourceKey<StructureProcessorList> =
        ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation("minecraft", "empty"))
    val EMPTY_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation("minecraft", "empty"))
    val DEATH_BODY_CHEST_POOL: ResourceKey<StructureTemplatePool> =
        ResourceKey.create(Registries.TEMPLATE_POOL, id("death_body_chest"))

    fun bootstrap(context: BootstapContext<StructureTemplatePool>) {
        val processors = context.lookup(Registries.PROCESSOR_LIST)
        val emptyProcessors = processors.getOrThrow(EMPTY_PROCESSOR_LIST)
        val fallback = context.lookup(Registries.TEMPLATE_POOL).getOrThrow(EMPTY_POOL)
        val elements = listOf(
            Pair.of(
                SinglePoolElement.single(id("death_body_chest").toString(), emptyProcessors)
                    .apply(StructureTemplatePool.Projection.RIGID) as StructurePoolElement, 1
            )
        )

        context.register(DEATH_BODY_CHEST_POOL, StructureTemplatePool(fallback, elements))
    }
}