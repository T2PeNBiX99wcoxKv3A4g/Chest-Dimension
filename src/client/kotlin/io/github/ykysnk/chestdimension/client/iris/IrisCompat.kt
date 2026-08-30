package io.github.ykysnk.chestdimension.client.iris

import io.github.ykysnk.chestdimension.compat.ModCompat
import io.github.ykysnk.chestdimension.utils.SimpleResourceLocation
import net.irisshaders.iris.Iris
import net.irisshaders.iris.uniforms.SystemTimeUniforms

object IrisCompat : ModCompat("iris") {
    private const val MOD_ID = "chest-dimension"
    fun getCurrentPack() = Iris.getCurrentPack()
    val frameTimeCounter
        get() = SystemTimeUniforms.TIMER.frameTimeCounter

    override fun initialize() {
        IrisBlockRegistry.register(
            SimpleResourceLocation("end_portal"),
            SimpleResourceLocation(MOD_ID, "teleport_door")
        )
    }
}