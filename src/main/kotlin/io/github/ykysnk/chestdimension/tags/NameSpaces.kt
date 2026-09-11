@file:Suppress("unused")

package io.github.ykysnk.chestdimension.tags

import io.github.ykysnk.chestdimension.utils.SimpleResourceLocation
import net.minecraft.resources.ResourceLocation

enum class NameSpaces(val id: String) {
    MOD("chest-dimension"),
    FORGE("c"),
    MINECRAFT("minecraft");

    fun path(name: String) = ResourceLocation(id, name)
    fun pathSimple(name: String) = SimpleResourceLocation(id, name)

    override fun toString(): String = id
    operator fun invoke() = id
    operator fun invoke(name: String) = path(name)
}