@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <T, R : T> MutableList<T>.addAndGet(func: () -> R): R {
    val item = func()
    add(item)
    return item
}

fun <T, R : T> MutableList<T>.addAndGet(value: R): R {
    add(value)
    return value
}