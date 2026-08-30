package io.github.ykysnk.chestdimension.client.compat

import io.github.ykysnk.chestdimension.compat.ModCompat
import net.irisshaders.iris.Iris

object IrisCompat : ModCompat("iris") {
    fun getCurrentPack() = Iris.getCurrentPack()
}