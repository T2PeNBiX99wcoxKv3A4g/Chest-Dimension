package io.github.ykysnk.chestdimension.client.iris

import io.github.ykysnk.chestdimension.compat.ModMixinConfigPlugin

class IrisMixinPlugin : ModMixinConfigPlugin() {
    override fun isModLoaded() = IrisCompat.isModLoaded
}