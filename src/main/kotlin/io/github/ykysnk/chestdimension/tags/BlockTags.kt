@file:Suppress("SameParameterValue")

package io.github.ykysnk.chestdimension.tags

import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

object BlockTags : RegistryHelper<TagKey<Block>>() {
    private fun bind(location: ResourceLocation): TagKey<Block> =
        register(TagKey.create(Registries.BLOCK, location))

    @Suppress("unused")
    private fun bind(name: String): TagKey<Block> = bind(NameSpaces.MOD(name))

    val GLASS_PANES = bind(NameSpaces.FORGE("glass_panes"))
}