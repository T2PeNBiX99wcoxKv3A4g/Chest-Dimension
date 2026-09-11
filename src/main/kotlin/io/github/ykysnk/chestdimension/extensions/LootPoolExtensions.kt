@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.entries.TagEntry
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// Copy form Standard.kt
@OptIn(ExperimentalContracts::class)
inline fun LootPool.Builder.build(block: LootPool.Builder.() -> Unit): LootPool.Builder {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}

inline fun LootPool.Builder.add(block: () -> LootPoolEntryContainer.Builder<*>): LootPool.Builder = add(block())

inline fun LootPool.Builder.lootTableItem(
    item: ItemLike,
    block: LootPoolSingletonContainer.Builder<*>.() -> Unit
): LootPool.Builder =
    add(LootItem.lootTableItem(item).build(block))

inline fun LootPool.Builder.expandTag(
    tag: TagKey<Item>,
    block: LootPoolSingletonContainer.Builder<*>.() -> Unit
): LootPool.Builder =
    add(TagEntry.expandTag(tag).build(block))