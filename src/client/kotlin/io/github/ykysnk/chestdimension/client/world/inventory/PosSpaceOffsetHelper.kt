package io.github.ykysnk.chestdimension.client.world.inventory

class PosSpaceOffsetHelper(private val initValue: Int, private val offset: Int) {
    private var count = 0

    fun get(): Int = initValue + offset * count

    fun next(): Int {
        count++
        return get()
    }
}