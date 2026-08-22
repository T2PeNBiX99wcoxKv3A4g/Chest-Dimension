@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

fun Vec3i.toVec3() = Vec3(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5)