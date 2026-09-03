package io.github.ykysnk.chestdimension.config

import io.github.ykysnk.chestdimension.id
import me.fzzyhmstrs.fzzy_config.config.Config

class MainConfig : Config(id("main_config")) {
    var teleportDoorTeleportToUndefined = true
    var automaticCloseTeleportDoor = false
}