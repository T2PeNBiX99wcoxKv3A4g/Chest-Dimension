@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

fun <K, V, R : V> MutableMap<K, V>.putAndGet(key: K, value: () -> R): R = putAndGet(key, value())

fun <K, V, R : V> MutableMap<K, V>.putAndGet(key: K, value: R): R {
    put(key, value)
    return value
}