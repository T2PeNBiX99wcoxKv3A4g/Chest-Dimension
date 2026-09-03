package io.github.ykysnk.chestdimension.config

import io.github.ykysnk.chestdimension.id
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.config.Config

class MainConfig : Config(id("main_config")) {
    companion object {
        val INSTANCE = ConfigApi.registerAndLoadConfig(::MainConfig)
    }

    val teleportDoorTeleportToUndefined = true;
}