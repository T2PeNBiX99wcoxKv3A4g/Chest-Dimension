package io.github.ykysnk.chestdimension.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi

object Configs {
    val mainConfig = ConfigApi.registerAndLoadConfig(::MainConfig)
}