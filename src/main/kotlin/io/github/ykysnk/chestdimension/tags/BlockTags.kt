package io.github.ykysnk.chestdimension.tags

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

object BlockTags : RegistryHelper<TagKey<Block>>() {
    private fun create(location: ResourceLocation): TagKey<Block> =
        register(TagKey.create(Registries.BLOCK, location))

    @Suppress("unused")
    private fun create(namespace: NameSpaces, name: String): TagKey<Block> = create(id(namespace, name))

    @Suppress("unused")
    private fun create(name: String): TagKey<Block> = create(id(name))
}