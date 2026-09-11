package io.github.ykysnk.chestdimension.extensions

import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
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