package io.github.ykysnk.chestdimension.extensions

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T : LootPoolSingletonContainer.Builder<T>> LootPoolSingletonContainer.Builder<T>.build(block: LootPoolSingletonContainer.Builder<T>.() -> Unit): LootPoolSingletonContainer.Builder<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}