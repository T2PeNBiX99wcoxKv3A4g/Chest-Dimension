package io.github.ykysnk.chestdimension.config

import io.github.ykysnk.chestdimension.NameSpaces
import me.fzzyhmstrs.fzzy_config.config.Config

class MainConfig : Config(NameSpaces.MOD("main_config")) {
    var teleportDoorTeleportToUndefined = true
    var automaticCloseTeleportDoor = false
}