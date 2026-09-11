package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items as MCItems

object ItemGroups {
    val CHEST_DIMENSION: CreativeModeTab = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        id("item_group"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.${NameSpaces.MOD}"))
            .icon { ItemStack(Items.CHEST_DIMENSION) }
            .displayItems { _, entries -> Items.all.forEach { entries.accept(it) } }
            .build()
    )

    init {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register {
            it.accept(Items.CHEST_PLATFORM)
            it.accept(Items.CHEST_PLATFORM_WALL)
            it.accept(Items.CHEST_PLATFORM_FENCE)
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register {
            it.addAfter(MCItems.ENDER_CHEST, Items.CHEST_DIMENSION)
        }
    }
}