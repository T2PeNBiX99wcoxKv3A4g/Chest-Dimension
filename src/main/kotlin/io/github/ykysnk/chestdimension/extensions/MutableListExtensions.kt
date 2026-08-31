@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <T> MutableList<T>.addAndGet(func: () -> T): T {
    val item = func()
    add(item)
    return item
}

fun <T> MutableList<T>.addAndGet(value: T): T {
    add(value)
    return value
}