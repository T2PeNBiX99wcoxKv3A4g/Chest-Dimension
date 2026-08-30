@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.utils

class SimpleResourceLocation(private val setNamespace: String, private val setPath: String) {
    constructor(setPath: String) : this("minecraft", setPath)

    val namespace: String
        get() = setNamespace
    val path: String
        get() = setPath

    override fun toString(): String {
        return "$namespace:$path"
    }
}