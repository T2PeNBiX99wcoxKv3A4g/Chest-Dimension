package io.github.ykysnk.chestdimension.level

import net.minecraft.server.level.ServerLevel
import java.util.*

data class LoadedChestWorld(val uuid: UUID, val level: ServerLevel)
