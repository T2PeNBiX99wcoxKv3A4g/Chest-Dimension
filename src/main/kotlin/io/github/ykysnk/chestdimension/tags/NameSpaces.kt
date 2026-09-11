@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.tags

import io.github.ykysnk.chestdimension.utils.SimpleResourceLocation
import net.minecraft.resources.ResourceLocation

enum class NameSpaces(val id: String) {
    MOD("chest-dimension"),
    FORGE("c"),
    MINECRAFT("minecraft");

    fun path(path: String) = ResourceLocation(id, path)
    fun pathSimple(name: String) = SimpleResourceLocation(id, name)

    override fun toString(): String = id
    operator fun invoke() = id
    operator fun invoke(path: String) = path(path)
}