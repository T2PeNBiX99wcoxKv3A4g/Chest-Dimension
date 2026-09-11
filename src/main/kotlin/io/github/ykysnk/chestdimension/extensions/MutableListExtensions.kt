@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <T, R : T> MutableList<T>.addAndGet(value: () -> R): R {
    val item = value()
    add(item)
    return item
}

fun <T, R : T> MutableList<T>.addAndGet(value: R): R {
    add(value)
    return value
}