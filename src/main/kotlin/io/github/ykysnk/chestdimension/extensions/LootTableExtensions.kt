package io.github.ykysnk.chestdimension.extensions

import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// Copy form Standard.kt
@OptIn(ExperimentalContracts::class)
inline fun LootTable.Builder.build(block: LootTable.Builder.() -> Unit): LootTable.Builder {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}

inline fun LootTable.Builder.withPool(block: LootPool.Builder.() -> Unit): LootTable.Builder {
    return withPool(LootPool.lootPool().build(block))
}