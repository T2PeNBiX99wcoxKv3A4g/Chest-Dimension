package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.extensions.addAndGet

open class RegistryHelper<T> {
    private val mutableAll = mutableListOf<T>()

    @Suppress("unused")
    val all: List<T>
        get() = mutableAll.toList()

    protected fun <R : T> register(value: R): R = mutableAll.addAndGet(value)

    protected fun <R : T> register(func: () -> R): R = mutableAll.addAndGet(func)
}