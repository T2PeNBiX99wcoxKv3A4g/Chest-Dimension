package io.github.ykysnk.chestdimension.level

import kotlinx.serialization.Serializable

@Serializable
data class Levels(var levels: HashMap<String, LevelData> = HashMap())
