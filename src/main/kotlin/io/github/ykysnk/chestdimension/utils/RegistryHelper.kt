package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.extensions.addAndGet

open class RegistryHelper<T> {
    private val mutableAll = mutableListOf<T>()

    @Suppress("unused")
    val all: List<T>
        get() = mutableAll.toList()

    protected open fun register(func: () -> T) = mutableAll.addAndGet(func)

    protected open fun register(value: T) = mutableAll.addAndGet(value)
}