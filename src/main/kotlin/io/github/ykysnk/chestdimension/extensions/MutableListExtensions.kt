@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <T> MutableList<T>.funcAndListAdd(func: () -> T): T {
    val item = func()
    add(item)
    return item
}