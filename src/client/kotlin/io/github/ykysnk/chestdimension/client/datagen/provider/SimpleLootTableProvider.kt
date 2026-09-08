package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.level.levelgen.structure.LootTables
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import java.util.function.BiConsumer
import net.minecraft.world.item.Items as MCItems

class SimpleLootTableProvider(output: FabricDataOutput) :
    SimpleFabricLootTableProvider(output, LootContextParamSets.CHEST) {
    override fun generate(output: BiConsumer<ResourceLocation, LootTable.Builder>) {
        output.accept(
            LootTables.DEATH_BODY_CHEST,
            LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(UniformGenerator.between(1.0f, 3.0f)).add(
                    LootItem.lootTableItem(MCItems.IRON_INGOT).setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 5.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.BREAD).setWeight(8)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.GOLD_NUGGET).setWeight(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.TORCH).setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 20.0f)))
                )
            )
        )
        output.accept(
            LootTables.SMALL_SHELTER,
            LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(UniformGenerator.between(3.0f, 6.0f)).add(
                    LootItem.lootTableItem(MCItems.IRON_INGOT).setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 5.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.BREAD).setWeight(8)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 20.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.GOLD_NUGGET).setWeight(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.OAK_LOG).setWeight(30)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0f, 10.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.DIAMOND).setWeight(2)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))
                ).add(
                    LootItem.lootTableItem(MCItems.TORCH).setWeight(20)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(32.0f, 64.0f)))
                )
            )
        )
    }
}