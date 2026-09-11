@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <T, R : T> MutableList<T>.addAndGet(value: () -> R): R = addAndGet(value())

fun <T, R : T> MutableList<T>.addAndGet(value: R): R {
    add(value)
    return value
}