package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.extensions.funcAndListAdd

open class RegistryHelper<T> {
    private val mutableAll = mutableListOf<T>()

    @Suppress("unused")
    val all: List<T>
        get() = mutableAll.toList()

    protected open fun register(func: () -> T) = mutableAll.funcAndListAdd(func)
}