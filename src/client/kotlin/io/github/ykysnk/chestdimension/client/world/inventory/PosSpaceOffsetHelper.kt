package io.github.ykysnk.chestdimension.client.world.inventory

class PosSpaceOffsetHelper(private val initValue: Int, private val offset: Int) {
    private var count = 0

    fun next(): Int {
        val get = offset * count
        count++
        return initValue + get
    }
}