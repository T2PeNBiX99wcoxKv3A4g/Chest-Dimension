package io.github.ykysnk.chestdimension.create

import io.github.ykysnk.chestdimension.compat.ModMixinConfigPlugin

class CreateMixinPlugin : ModMixinConfigPlugin() {
    override fun isModLoaded() = CreateCompat.isModLoaded
}