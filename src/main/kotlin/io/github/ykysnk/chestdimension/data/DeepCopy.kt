package io.github.ykysnk.chestdimension.data

interface DeepCopy<T> {
    fun deepCopy(): T
}