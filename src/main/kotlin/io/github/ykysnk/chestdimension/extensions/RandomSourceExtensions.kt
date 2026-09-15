package io.github.ykysnk.chestdimension.extensions

import net.minecraft.util.RandomSource

fun RandomSource.chance(percentage: Int): Boolean = nextInt(100) < percentage