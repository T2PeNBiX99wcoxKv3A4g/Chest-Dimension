package io.github.ykysnk.chestdimension.level

import net.minecraft.world.level.levelgen.WorldOptions

object ChestServerLevelContext {
    @JvmField
    val worldOptions = ThreadLocal<WorldOptions?>()
}