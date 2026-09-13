package io.github.ykysnk.chestdimension.client.iris

import io.github.ykysnk.chestdimension.NameSpaces
import io.github.ykysnk.chestdimension.compat.ModCompat
import net.irisshaders.iris.Iris
import net.irisshaders.iris.uniforms.SystemTimeUniforms

object IrisCompat : ModCompat("iris") {
    fun getCurrentPack() = Iris.getCurrentPack()
    val frameTimeCounter
        get() = SystemTimeUniforms.TIMER.frameTimeCounter

    override fun initialize() {
        IrisBlockRegistry.apply {
            register(NameSpaces.MINECRAFT.pathSimple("end_portal"), NameSpaces.MOD.pathSimple("teleport_door"))
            register(NameSpaces.MINECRAFT.pathSimple("glass"), NameSpaces.MOD.pathSimple("blast_resistant_glass"))
            register(
                NameSpaces.MINECRAFT.pathSimple("glass_pane"),
                NameSpaces.MOD.pathSimple("blast_resistant_glass_pane")
            )
        }
    }
}