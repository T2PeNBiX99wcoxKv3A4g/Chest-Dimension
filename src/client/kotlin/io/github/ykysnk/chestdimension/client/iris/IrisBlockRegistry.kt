@file:Suppress("unused")

package io.github.ykysnk.chestdimension.client.iris

import io.github.ykysnk.chestdimension.utils.SimpleResourceLocation

object IrisBlockRegistry {
    private val sameEffectBlocks = mutableMapOf<String, HashSet<SimpleResourceLocation>>()

    fun getList(findName: String): Set<SimpleResourceLocation> {
        return sameEffectBlocks[findName]?.toSet() ?: setOf()
    }

    fun register(mcBlockId: SimpleResourceLocation, modBlockId: SimpleResourceLocation) {
        sameEffectBlocks.getOrPut(mcBlockId.toString()) { hashSetOf() }.add(modBlockId)
    }
}