package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.extensions.expandTag
import io.github.ykysnk.chestdimension.extensions.lootTableItem
import io.github.ykysnk.chestdimension.extensions.withPool
import io.github.ykysnk.chestdimension.level.levelgen.structure.LootTables
import io.github.ykysnk.chestdimension.tags.ItemTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
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
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(1.0f, 3.0f))
                lootTableItem(MCItems.IRON_INGOT) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 5.0f)))
                }
                lootTableItem(MCItems.BREAD) {
                    setWeight(8)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                }
                lootTableItem(MCItems.GOLD_NUGGET) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f)))
                }
                lootTableItem(MCItems.TORCH) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 20.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ENTRANCE_STORAGE,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                lootTableItem(MCItems.IRON_INGOT) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 5.0f)))
                }
                lootTableItem(MCItems.BREAD) {
                    setWeight(8)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 20.0f)))
                }
                lootTableItem(MCItems.GOLD_NUGGET) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f)))
                }
                lootTableItem(MCItems.OAK_LOG) {
                    setWeight(30)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0f, 10.0f)))
                }
                lootTableItem(MCItems.DIAMOND) {
                    setWeight(2)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))
                }
                lootTableItem(MCItems.TORCH) {
                    setWeight(20)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(32.0f, 64.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ROOM_STORAGE,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                lootTableItem(MCItems.IRON_INGOT) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 10.0f)))
                }
                lootTableItem(MCItems.BREAD) {
                    setWeight(8)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 40.0f)))
                }
                lootTableItem(MCItems.GOLD_NUGGET) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 16.0f)))
                }
                lootTableItem(MCItems.OAK_LOG) {
                    setWeight(30)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0f, 20.0f)))
                }
                lootTableItem(MCItems.DIAMOND) {
                    setWeight(2)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 4.0f)))
                }
                lootTableItem(MCItems.TORCH) {
                    setWeight(20)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(32.0f, 128.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ROOM_FARM,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                lootTableItem(MCItems.WHEAT_SEEDS) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 20.0f)))
                }
                lootTableItem(MCItems.WHEAT) {
                    setWeight(8)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 20.0f)))
                }
                lootTableItem(MCItems.POTATO) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 15.0f)))
                }
                lootTableItem(MCItems.POISONOUS_POTATO) {
                    setWeight(6)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 15.0f)))
                }
                lootTableItem(MCItems.CARROT) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 15.0f)))
                }
                lootTableItem(MCItems.GOLDEN_CARROT) {
                    setWeight(2)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ROOM_KITCHEN,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                expandTag(ItemTags.BASEMENT_RAW_MEAT_FOOD) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 20.0f)))
                }
                expandTag(ItemTags.BASEMENT_COOKED_MEAT_FOOD) {
                    setWeight(8)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 20.0f)))
                }
                expandTag(ItemTags.BASEMENT_RAW_VEGETABLES_FOOD) {
                    setWeight(6)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 15.0f)))
                }
                expandTag(ItemTags.BASEMENT_COOKED_VEGETABLES_FOOD) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 15.0f)))
                }
                lootTableItem(MCItems.GOLDEN_CARROT) {
                    setWeight(2)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ROOM_SMELTERY,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                lootTableItem(MCItems.COPPER_INGOT) {
                    setWeight(20)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 10.0f)))
                }
                lootTableItem(MCItems.IRON_INGOT) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 5.0f)))
                }
                lootTableItem(MCItems.GOLD_INGOT) {
                    setWeight(5)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                }
                lootTableItem(MCItems.DIAMOND) {
                    setWeight(2)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))
                }
            }
        )
        output.accept(
            LootTables.SMALL_SHELTER_ROOM_ENCHANTING,
            LootTable.lootTable().withPool {
                setRolls(UniformGenerator.between(3.0f, 6.0f))
                lootTableItem(MCItems.LAPIS_LAZULI) {
                    setWeight(10)
                    apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 10.0f)))
                }
                lootTableItem(MCItems.BOOK) {
                    setWeight(5)
                    apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                }
            })
    }
}