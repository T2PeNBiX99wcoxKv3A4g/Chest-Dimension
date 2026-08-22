package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class Levels(var levels: HashMap<String, LevelData> = HashMap()) {
    fun deepCopy() = Levels(HashMap(levels))
}
