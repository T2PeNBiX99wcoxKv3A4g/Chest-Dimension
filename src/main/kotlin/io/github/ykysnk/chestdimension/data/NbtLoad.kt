package io.github.ykysnk.chestdimension.data

import net.minecraft.nbt.CompoundTag

interface NbtLoad<T> {
    fun load(tag: CompoundTag): T
}