@file:Suppress("SameParameterValue")

package io.github.ykysnk.chestdimension.tags

import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ItemTags : RegistryHelper<TagKey<Item>>() {
    private fun bind(location: ResourceLocation): TagKey<Item> =
        register(TagKey.create(Registries.ITEM, location))

    @Suppress("unused")
    private fun bind(name: String): TagKey<Item> = bind(NameSpaces.MOD(name))

    val BASEMENT_RAW_MEAT_FOOD = bind("basement_raw_meat_food")
    val BASEMENT_COOKED_MEAT_FOOD = bind("basement_cooked_meat_food")
    val BASEMENT_RAW_VEGETABLES_FOOD = bind("basement_raw_vegetables_food")
    val BASEMENT_COOKED_VEGETABLES_FOOD = bind("basement_cooked_vegetables_food")
}