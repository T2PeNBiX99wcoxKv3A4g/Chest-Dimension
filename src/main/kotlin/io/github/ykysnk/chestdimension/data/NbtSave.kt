package io.github.ykysnk.chestdimension.data

import net.minecraft.nbt.CompoundTag

interface NbtSave {
    fun save(): CompoundTag
}