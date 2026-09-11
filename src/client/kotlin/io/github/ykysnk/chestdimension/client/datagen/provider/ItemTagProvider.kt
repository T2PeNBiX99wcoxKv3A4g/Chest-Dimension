package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.tags.ItemTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture
import net.minecraft.tags.ItemTags as MCItemTags
import net.minecraft.world.item.Items as MCItems

class ItemTagProvider(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    FabricTagProvider.ItemTagProvider(output, registries) {
    override fun addTags(provider: HolderLookup.Provider) {
        getOrCreateTagBuilder(MCItemTags.WALLS).apply {
            add(Items.CHEST_PLATFORM_WALL)
        }
        getOrCreateTagBuilder(MCItemTags.FENCES).apply {
            add(Items.CHEST_PLATFORM_FENCE)
        }
        getOrCreateTagBuilder(MCItemTags.WOODEN_PRESSURE_PLATES).apply {
            add(Items.TELEPORT_PRESSURE_PLATE)
        }
        getOrCreateTagBuilder(ItemTags.BASEMENT_RAW_MEAT_FOOD).apply {
            add(MCItems.CHICKEN)
            add(MCItems.RABBIT)
            add(MCItems.BEEF)
            add(MCItems.PORKCHOP)
        }
        getOrCreateTagBuilder(ItemTags.BASEMENT_COOKED_MEAT_FOOD).apply {
            add(MCItems.COOKED_CHICKEN)
            add(MCItems.COOKED_RABBIT)
            add(MCItems.COOKED_BEEF)
            add(MCItems.COOKED_PORKCHOP)
        }
        getOrCreateTagBuilder(ItemTags.BASEMENT_RAW_VEGETABLES_FOOD).apply {
            add(MCItems.WHEAT)
            add(MCItems.WHEAT_SEEDS)
            add(MCItems.POTATO)
            add(MCItems.BEETROOT_SEEDS)
            add(MCItems.CARROT)
            add(MCItems.POISONOUS_POTATO)
        }
        getOrCreateTagBuilder(ItemTags.BASEMENT_COOKED_VEGETABLES_FOOD).apply {
            add(MCItems.BREAD)
            add(MCItems.BAKED_POTATO)
            add(MCItems.BEETROOT)
        }
    }
}